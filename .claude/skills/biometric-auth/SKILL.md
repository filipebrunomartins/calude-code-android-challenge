---
name: biometric-auth
description: Passo a passo de uso da AndroidX Biometric Library no MovieFlux — verificação de disponibilidade, BiometricPrompt, fallback quando a biometria falha ou não está disponível, e o requisito de FragmentActivity. Use ao implementar ou revisar o fluxo de login/biometria em feature-auth.
when_to_use: Ao criar ou editar BiometricGateScreen, AuthViewModel, ou qualquer código que decida se/como pedir biometria.
---

# AndroidX Biometric no MovieFlux

## Pré-requisito de Activity

`BiometricPrompt` exige um `FragmentActivity` (ou `AppCompatActivity`, que estende `FragmentActivity`). **`MainActivity` do MovieFlux deve estender `FragmentActivity`**, não `ComponentActivity` puro — `setContent { }` do Compose continua funcionando normalmente sobre `FragmentActivity`. Dentro de um Composable, obter a Activity com `LocalContext.current as FragmentActivity` (ou um helper de extensão que já faz esse cast/`unwrap`).

## Passo 1 — checar disponibilidade antes de oferecer o toggle

Depois do primeiro login bem-sucedido, antes de perguntar "deseja ativar biometria?", checar `BIOMETRIC_STRONG` primeiro e cair para `BIOMETRIC_WEAK` se necessário — ver `BiometricCapabilityChecker.allowedAuthenticators()` em `feature-auth/presentation`:

```kotlin
val biometricManager = BiometricManager.from(context)
val strongWithCredential = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
val allowedAuthenticators = when {
    biometricManager.canAuthenticate(strongWithCredential) == BiometricManager.BIOMETRIC_SUCCESS -> strongWithCredential
    biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS ->
        BiometricManager.Authenticators.BIOMETRIC_WEAK
    else -> null // não oferecer o toggle; app segue só com senha
}
```

**Por que o fallback para `BIOMETRIC_WEAK`:** validado em dispositivo físico real (ASUS_A001D, Android 9 / API 28) — o sensor de digital tem biometria cadastrada e funcional, mas o framework não o certifica como Class 3, então `canAuthenticate(BIOMETRIC_STRONG)` retorna falha mesmo assim. Exigir só `BIOMETRIC_STRONG` deixaria aparelhos mais antigos (ainda dentro do `minSdk 26` do projeto) sem poder usar biometria nenhuma. Nunca mostrar o diálogo de "ativar biometria" se `allowedAuthenticators()` retornar `null` — é um dos critérios de avaliação explícitos do desafio ("implementação correta da API de Biometria... e fallback em caso de falha").

## Passo 2 — construir o BiometricPrompt com o nível detectado

```kotlin
val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Entrar no MovieFlux")
    .setSubtitle("Use sua biometria para continuar")
    .setAllowedAuthenticators(allowedAuthenticators) // o mesmo valor detectado no Passo 1

// DEVICE_CREDENTIAL e setNegativeButtonText são mutuamente exclusivos na API do AndroidX
// Biometric — só usar o botão negativo quando DEVICE_CREDENTIAL não está incluído.
if (allowedAuthenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL == 0) {
    promptInfoBuilder.setNegativeButtonText("Usar senha")
}
```

- `DEVICE_CREDENTIAL` (PIN/padrão/senha do aparelho) combinado com `BIOMETRIC_STRONG` dá um fallback nativo do sistema. **`BIOMETRIC_WEAK` não pode ser combinado com `DEVICE_CREDENTIAL`** na mesma prompt (a API rejeita essa combinação) — por isso o `PromptInfo` precisa ser montado com o mesmo valor de `allowedAuthenticators` calculado no Passo 1, nunca hardcoded.

## Passo 3 — tratar o resultado (segundo nível de fallback)

```kotlin
val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        // navega para Home
    }
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        when (errorCode) {
            BiometricPrompt.ERROR_LOCKOUT,
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_USER_CANCELED -> // segundo fallback: volta para tela de senha mockada do app
            else -> // mostra mensagem de erro genérica e oferece tentar de novo ou usar senha
        }
    }
    override fun onAuthenticationFailed() {
        // biometria não reconhecida (não é erro fatal) — deixa o usuário tentar de novo, o próprio prompt já lida com isso
    }
})
```

O fallback final, em qualquer caso de erro não recuperável, é sempre a tela de login mockado do app (usuário/senha) — o app nunca deve travar o usuário fora por falha de biometria.

## Onde isso vive na arquitetura

- `BiometricManager`/`BiometricPrompt` são chamadas de **UI/Activity**, então ficam em `feature-auth/presentation` (ex. `BiometricGateScreen.kt`), nunca no `domain` (que é Kotlin puro) nem no `data`.
- O `AuthViewModel` só conhece o *resultado* (sucesso/erro/cancelado) e decide o próximo passo via use cases (`ObserveSessionUseCase`, etc.) — ele não importa `BiometricPrompt` diretamente. Isolar assim facilita testar a lógica de decisão sem precisar simular a API real do Android (ver skill `android-testing` — usar um `BiometricCapabilityChecker` fake-ável para testar `AuthViewModel`).

## Persistência da preferência de biometria

O toggle "biometria ativa/desativa" é lido/gravado via `SecureStorage` (EncryptedSharedPreferences) em `core-security`, nunca em `SharedPreferences` comum — é dado de configuração de segurança do usuário.
