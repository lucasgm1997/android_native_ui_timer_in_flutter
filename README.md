# Native Timer - Flutter com Timer 100% Nativo

Este projeto demonstra como implementar um timer que roda completamente no código nativo (Android/iOS), continuando a funcionar mesmo com o aplicativo Flutter fechado ou em segundo plano.

## 🚀 Características

- **Timer 100% Nativo**: Toda a lógica do timer roda no código nativo
- **Funciona em Background**: Continua funcionando com o app fechado
- **Notificações Locais**: Notifica o usuário quando o timer termina
- **Serviços Nativos**: Usa foreground services (Android) e background tasks (iOS)
- **Comunicação via MethodChannel**: Interface simples entre Flutter e código nativo

## 📱 Funcionalidades

### Android
- Foreground Service para manter o timer ativo
- Notificações persistentes mostrando tempo restante
- Notificação de conclusão com som e vibração
- Suporte a Android 6.0+ (API 23+)

### iOS
- Background App Refresh para continuidade
- Local Notifications para conclusão
- Background Tasks (iOS 13+)
- Suporte a iOS 10.0+

## 🛠️ Configuração do Projeto

### Pré-requisitos

- Flutter SDK 3.3.0+
- Android Studio / Xcode
- Dispositivo físico ou emulador

### 1. Dependências

O projeto já inclui as dependências necessárias no `pubspec.yaml`:

```yaml
dependencies:
  flutter:
    sdk: flutter
  cupertino_icons: ^1.0.8
  flutter_local_notifications: ^18.0.1

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^4.0.0
```

### 2. Configuração Android

#### Permissões (AndroidManifest.xml)
As seguintes permissões foram adicionadas:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.VIBRATE" />
```

#### Serviço (AndroidManifest.xml)
Declaração do TimerService:

```xml
<service
    android:name=".TimerService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="timer" />
</service>
```

#### Arquivos Nativos Android
- `MainActivity.kt`: Implementa MethodChannel e gerencia o serviço
- `TimerService.kt`: Serviço em foreground que executa o timer

### 3. Configuração iOS

#### Info.plist
Adicionadas as seguintes configurações:

```xml
<!-- Background modes -->
<key>UIBackgroundModes</key>
<array>
    <string>background-app-refresh</string>
    <string>background-processing</string>
</array>

<!-- Background task identifier -->
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.example.native_timer.background</string>
</array>
```

#### Arquivo Nativo iOS
- `AppDelegate.swift`: Implementa MethodChannel, background tasks e notificações

### 4. Instalação

```bash
# Clone o repositório
git clone <repository-url>
cd native_timer

# Instale as dependências
flutter pub get

# Execute no dispositivo/emulador
flutter run
```

## 🔧 Como Funciona

### Arquitetura

```
Flutter App (UI)
       ↕ MethodChannel
Native Code (Timer Logic)
       ↕ Platform Services
OS (Notifications, Background)
```

### Fluxo de Execução

1. **Início**: Flutter chama `startTimer` via MethodChannel
2. **Nativo**: Código nativo inicia timer e serviços de background
3. **Background**: Timer continua rodando independentemente do Flutter
4. **Conclusão**: Sistema nativo envia notificação local
5. **Status**: Flutter pode consultar status via `isTimerRunning`

### MethodChannel Interface

```dart
// Iniciar timer
await platform.invokeMethod('startTimer', {'duration': 60});

// Parar timer
await platform.invokeMethod('stopTimer');

// Verificar status
final isRunning = await platform.invokeMethod('isTimerRunning');
```

## 📋 Implementação Detalhada

### Android - TimerService

```kotlin
class TimerService : Service() {
    // Foreground service que mantém o timer ativo
    // CountDownTimer para contagem regressiva
    // NotificationManager para notificações
    // Persiste mesmo com app fechado
}
```

### iOS - AppDelegate

```swift
class AppDelegate: FlutterAppDelegate {
    // Timer nativo do iOS
    // Background tasks para continuidade
    // UNUserNotificationCenter para notificações
    // Background app refresh
}
```

### Flutter - Interface

```dart
class NativeTimerPage extends StatefulWidget {
    // Interface de usuário
    // MethodChannel para comunicação
    // Gerenciamento de estado local
    // Feedback visual do status
}
```

## 🎯 Recursos Implementados

### ✅ Funcionalidades Principais
- [x] Timer nativo Android (Foreground Service)
- [x] Timer nativo iOS (Background Tasks)
- [x] Notificações de conclusão
- [x] Interface Flutter para controle
- [x] Persistência com app fechado
- [x] Comunicação via MethodChannel

### ✅ Recursos Android
- [x] Foreground Service com notificação persistente
- [x] Atualização em tempo real do tempo restante
- [x] Notificação de conclusão com som/vibração
- [x] Botão para parar timer na notificação
- [x] Permissões adequadas para Android 13+

### ✅ Recursos iOS
- [x] Background App Refresh
- [x] Local Notifications
- [x] Background Tasks (iOS 13+)
- [x] Gerenciamento de badge
- [x] Timer nativo com NSTimer

## 🧪 Testando

### Teste Básico
1. Abra o app
2. Defina duração (ex: 10 segundos)
3. Toque "Iniciar Timer"
4. Feche o app completamente
5. Aguarde o tempo definido
6. Verifique se a notificação aparece

### Teste de Background
1. Inicie um timer longo (ex: 60 segundos)
2. Minimize o app
3. Verifique notificação persistente (Android)
4. Aguarde conclusão
5. Verifique notificação final

## 🔍 Troubleshooting

### Android
- **Permissões**: Certifique-se que notificações estão habilitadas
- **Battery Optimization**: Desabilite otimização de bateria para o app
- **Background Restrictions**: Verifique configurações de background

### iOS
- **Background App Refresh**: Habilite nas configurações do iOS
- **Notifications**: Permita notificações para o app
- **Simulator**: Algumas funcionalidades podem não funcionar no simulador

## 📚 Referências

- [Flutter Platform Channels](https://docs.flutter.dev/development/platform-integration/platform-channels)
- [Android Foreground Services](https://developer.android.com/guide/components/foreground-services)
- [iOS Background Tasks](https://developer.apple.com/documentation/backgroundtasks)
- [Flutter Local Notifications](https://pub.dev/packages/flutter_local_notifications)

## 🤝 Contribuição

Contribuições são bem-vindas! Por favor:

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

---

**Nota**: Este é um projeto de demonstração que mostra como implementar timers nativos em Flutter. Para uso em produção, considere adicionar tratamento de erros mais robusto, testes unitários e otimizações de performance.
