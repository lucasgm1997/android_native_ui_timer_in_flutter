# Exemplo de Uso - Native Timer

Este documento mostra exemplos práticos de como usar o timer nativo implementado.

## 🎯 Casos de Uso Comuns

### 1. Timer de Pomodoro (25 minutos)
```dart
// Iniciar sessão de trabalho de 25 minutos
await platform.invokeMethod('startTimer', {'duration': 1500}); // 25 * 60 = 1500 segundos
```

### 2. Timer de Descanso (5 minutos)
```dart
// Iniciar pausa de 5 minutos
await platform.invokeMethod('startTimer', {'duration': 300}); // 5 * 60 = 300 segundos
```

### 3. Timer de Exercício (30 segundos)
```dart
// Timer para exercícios intervalados
await platform.invokeMethod('startTimer', {'duration': 30});
```

### 4. Timer de Cozinha (10 minutos)
```dart
// Timer para cozinhar
await platform.invokeMethod('startTimer', {'duration': 600}); // 10 * 60 = 600 segundos
```

## 🔧 Implementação Personalizada

### Classe Timer Personalizada

```dart
class NativeTimerManager {
  static const platform = MethodChannel('com.example.native_timer/timer');
  
  // Iniciar timer com duração em minutos
  static Future<bool> startTimerMinutes(int minutes) async {
    try {
      await platform.invokeMethod('startTimer', {'duration': minutes * 60});
      return true;
    } catch (e) {
      print('Erro ao iniciar timer: $e');
      return false;
    }
  }
  
  // Iniciar timer com duração em segundos
  static Future<bool> startTimerSeconds(int seconds) async {
    try {
      await platform.invokeMethod('startTimer', {'duration': seconds});
      return true;
    } catch (e) {
      print('Erro ao iniciar timer: $e');
      return false;
    }
  }
  
  // Parar timer
  static Future<bool> stopTimer() async {
    try {
      await platform.invokeMethod('stopTimer');
      return true;
    } catch (e) {
      print('Erro ao parar timer: $e');
      return false;
    }
  }
  
  // Verificar se timer está rodando
  static Future<bool> isRunning() async {
    try {
      return await platform.invokeMethod('isTimerRunning');
    } catch (e) {
      print('Erro ao verificar status: $e');
      return false;
    }
  }
}
```

### Widget de Timer Personalizado

```dart
class CustomTimerWidget extends StatefulWidget {
  final int initialDuration;
  final String title;
  final VoidCallback? onComplete;
  
  const CustomTimerWidget({
    Key? key,
    required this.initialDuration,
    required this.title,
    this.onComplete,
  }) : super(key: key);
  
  @override
  State<CustomTimerWidget> createState() => _CustomTimerWidgetState();
}

class _CustomTimerWidgetState extends State<CustomTimerWidget> {
  bool _isRunning = false;
  
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Text(
              widget.title,
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 16),
            Text(
              '${widget.initialDuration ~/ 60}:${(widget.initialDuration % 60).toString().padLeft(2, '0')}',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: _isRunning ? null : _startTimer,
                  child: const Text('Iniciar'),
                ),
                ElevatedButton(
                  onPressed: _isRunning ? _stopTimer : null,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.red,
                    foregroundColor: Colors.white,
                  ),
                  child: const Text('Parar'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
  
  Future<void> _startTimer() async {
    final success = await NativeTimerManager.startTimerSeconds(widget.initialDuration);
    if (success) {
      setState(() => _isRunning = true);
    }
  }
  
  Future<void> _stopTimer() async {
    final success = await NativeTimerManager.stopTimer();
    if (success) {
      setState(() => _isRunning = false);
    }
  }
}
```

## 📱 Exemplos de Interface

### 1. Lista de Timers Predefinidos

```dart
class TimerPresetsList extends StatelessWidget {
  final List<TimerPreset> presets = [
    TimerPreset('Pomodoro', 1500, Icons.work),
    TimerPreset('Descanso Curto', 300, Icons.coffee),
    TimerPreset('Descanso Longo', 900, Icons.weekend),
    TimerPreset('Exercício', 30, Icons.fitness_center),
    TimerPreset('Meditação', 600, Icons.self_improvement),
  ];
  
  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: presets.length,
      itemBuilder: (context, index) {
        final preset = presets[index];
        return ListTile(
          leading: Icon(preset.icon),
          title: Text(preset.name),
          subtitle: Text('${preset.duration ~/ 60} minutos'),
          trailing: IconButton(
            icon: const Icon(Icons.play_arrow),
            onPressed: () => _startPresetTimer(preset),
          ),
        );
      },
    );
  }
  
  void _startPresetTimer(TimerPreset preset) async {
    await NativeTimerManager.startTimerSeconds(preset.duration);
  }
}

class TimerPreset {
  final String name;
  final int duration;
  final IconData icon;
  
  TimerPreset(this.name, this.duration, this.icon);
}
```

### 2. Timer com Picker de Tempo

```dart
class TimerPickerWidget extends StatefulWidget {
  @override
  State<TimerPickerWidget> createState() => _TimerPickerWidgetState();
}

class _TimerPickerWidgetState extends State<TimerPickerWidget> {
  int _hours = 0;
  int _minutes = 5;
  int _seconds = 0;
  
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            _buildTimePicker('Horas', _hours, 23, (value) => setState(() => _hours = value)),
            _buildTimePicker('Minutos', _minutes, 59, (value) => setState(() => _minutes = value)),
            _buildTimePicker('Segundos', _seconds, 59, (value) => setState(() => _seconds = value)),
          ],
        ),
        const SizedBox(height: 20),
        ElevatedButton(
          onPressed: _startCustomTimer,
          child: const Text('Iniciar Timer Personalizado'),
        ),
      ],
    );
  }
  
  Widget _buildTimePicker(String label, int value, int max, ValueChanged<int> onChanged) {
    return Column(
      children: [
        Text(label),
        SizedBox(
          width: 80,
          child: TextField(
            textAlign: TextAlign.center,
            keyboardType: TextInputType.number,
            controller: TextEditingController(text: value.toString()),
            onChanged: (text) {
              final newValue = int.tryParse(text) ?? 0;
              if (newValue >= 0 && newValue <= max) {
                onChanged(newValue);
              }
            },
          ),
        ),
      ],
    );
  }
  
  void _startCustomTimer() async {
    final totalSeconds = (_hours * 3600) + (_minutes * 60) + _seconds;
    if (totalSeconds > 0) {
      await NativeTimerManager.startTimerSeconds(totalSeconds);
    }
  }
}
```

## 🔔 Tratamento de Notificações

### Personalizar Notificações (Android)

Para personalizar as notificações no Android, edite o `TimerService.kt`:

```kotlin
private fun createForegroundNotification(totalSeconds: Int): Notification {
    return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("🔥 Timer Ativo")
        .setContentText("⏰ Timer de ${totalSeconds}s iniciado")
        .setSmallIcon(R.drawable.ic_timer) // Use seu próprio ícone
        .setColor(ContextCompat.getColor(this, R.color.primary_color))
        .setContentIntent(pendingIntent)
        .addAction(R.drawable.ic_stop, "Parar", stopPendingIntent)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()
}
```

### Personalizar Notificações (iOS)

Para personalizar as notificações no iOS, edite o `AppDelegate.swift`:

```swift
private func scheduleCompletionNotification(after seconds: Int) {
    let content = UNMutableNotificationContent()
    content.title = "🎉 Timer Concluído!"
    content.body = "✅ O timer foi finalizado com sucesso"
    content.sound = .default
    content.badge = 1
    
    // Adicionar ação personalizada
    let stopAction = UNNotificationAction(
        identifier: "STOP_ACTION",
        title: "OK",
        options: []
    )
    
    let category = UNNotificationCategory(
        identifier: "TIMER_CATEGORY",
        actions: [stopAction],
        intentIdentifiers: [],
        options: []
    )
    
    UNUserNotificationCenter.current().setNotificationCategories([category])
    content.categoryIdentifier = "TIMER_CATEGORY"
    
    let trigger = UNTimeIntervalNotificationTrigger(timeInterval: TimeInterval(seconds), repeats: false)
    let request = UNNotificationRequest(identifier: "timer_completion", content: content, trigger: trigger)
    
    UNUserNotificationCenter.current().add(request)
}
```

## 🧪 Testes de Integração

### Teste Básico de Funcionalidade

```dart
void main() {
  group('Native Timer Tests', () {
    test('should start timer successfully', () async {
      final result = await NativeTimerManager.startTimerSeconds(10);
      expect(result, true);
      
      final isRunning = await NativeTimerManager.isRunning();
      expect(isRunning, true);
    });
    
    test('should stop timer successfully', () async {
      await NativeTimerManager.startTimerSeconds(10);
      final result = await NativeTimerManager.stopTimer();
      expect(result, true);
      
      final isRunning = await NativeTimerManager.isRunning();
      expect(isRunning, false);
    });
  });
}
```

## 📊 Monitoramento e Analytics

### Adicionar Logs de Uso

```dart
class TimerAnalytics {
  static void logTimerStarted(int duration) {
    print('Timer iniciado: ${duration}s');
    // Adicionar analytics (Firebase, etc.)
  }
  
  static void logTimerCompleted(int duration) {
    print('Timer completado: ${duration}s');
    // Adicionar analytics
  }
  
  static void logTimerCancelled(int remainingTime) {
    print('Timer cancelado: ${remainingTime}s restantes');
    // Adicionar analytics
  }
}
```

## 🎨 Customização Visual

### Tema Personalizado

```dart
class TimerTheme {
  static ThemeData get theme => ThemeData(
    primarySwatch: Colors.orange,
    visualDensity: VisualDensity.adaptivePlatformDensity,
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(8),
        ),
      ),
    ),
    cardTheme: CardTheme(
      elevation: 4,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
      ),
    ),
  );
}
```

---

Este arquivo de exemplo mostra como estender e personalizar o timer nativo para diferentes casos de uso. Adapte conforme suas necessidades específicas! 