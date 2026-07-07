---
name: biometric-auth
description: Passo a passo de uso da AndroidX Biometric Library no MovieFlux — verificação de disponibilidade, BiometricPrompt, fallback quando a biometria falha ou não está disponível, e o requisito de FragmentActivity. Use ao implementar ou revisar o fluxo de login/biometria em feature-auth.
when_to_use: Ao criar ou editar BiometricGateScreen, AuthViewModel, ou qualquer código que decida se/como pedir biometria.
---

# AndroidX Biometric no MovieFlux

## Pré-requisito de Activity

`BiometricPrompt` exige um `FragmentActivity` (ou `AppCompatActivity`, que estende `FragmentActivity`). **`MainActivity` do MovieFlux deve estender `FragmentActivity`**, não `ComponentActivity` puro — `setContent { }` do Compose continua funcionando normalmente sobre `FragmentActivity`. Dentro de um Composable, obter a Activity com `LocalContext.current as FragmentActivity` (ou um helper de extensão que já faz esse cast/`unwrap`).

## Passo 1 — checar disponibilidade antes de oferecer o toggle

Depois do primeiro login bem-sucedido, antes de perguntar "deseja ativar biometria?", checar:

```kotlin
val biometricManager = BiometricManager.from(context)
when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
    BiometricManager.BIOMETRIC_SUCCESS -> // ok, pode oferecer o toggle
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> // não oferecer o toggle; app segue só com senha
    else -> // erro transitório — não oferecer agora, pode tentar de novo depois
}
```

Nunca mostrar o diálogo de "ativar biometria" se `canAuthenticate` não retornar `BIOMETRIC_SUCCESS` — é um dos critérios de avaliação explícitos do desafio ("implementação correta da API de Biometria... e fallback em caso de falha").

## Passo 2 — construir o BiometricPrompt com fallback

```kotlin
val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Entrar no MovieFlux")
    .setSubtitle("Use sua biometria para continuar")
    .setAllowedAuthenticators(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
    .build()
```

- `DEVICE_CREDENTIAL` (PIN/padrão/senha do aparelho) combinado com `BIOMETRIC_STRONG` dá um fallback nativo do sistema — **não é necessário (nem permitido) chamar `.setNegativeButtonText(...)` junto com `DEVICE_CREDENTIAL`**, os dois são mutuamente exclusivos na API do AndroidX Biometric (usar `setNegativeButtonText` só se optar por não incluir `DEVICE_CREDENTIAL`).

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
