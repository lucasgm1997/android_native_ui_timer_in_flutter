import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Native Timer',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const NativeTimerPage(),
    );
  }
}

class NativeTimerPage extends StatefulWidget {
  const NativeTimerPage({super.key});

  @override
  State<NativeTimerPage> createState() => _NativeTimerPageState();
}

class _NativeTimerPageState extends State<NativeTimerPage> {
  static const platform = MethodChannel('com.example.native_timer/timer');
  String _timeText = "00:00:00";
  bool _isRunning = false;

  @override
  void initState() {
    super.initState();
    platform.setMethodCallHandler(_handleMethodCall);
  }

  Future<void> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onTimerUpdate':
        final Map<String, dynamic> args =
            call.arguments.cast<String, dynamic>();
        setState(() {
          _timeText = args['timeText'] ?? "00:00:00";
          _isRunning = args['isRunning'] ?? false;
        });
        break;
      default:
        throw MissingPluginException();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('Native Timer'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _timeText,
              style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: _isRunning ? Colors.green : Colors.grey,
                  ),
            ),
            const SizedBox(height: 20),
            Text(
              _isRunning ? 'Timer is running' : 'Timer is stopped',
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: _isRunning ? Colors.green : Colors.grey,
                  ),
            ),
            const SizedBox(height: 40),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                border: Border.all(color: Colors.grey.shade300),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const SizedBox(
                height: 150,
                width: double.infinity,
                child: AndroidView(
                  viewType: 'NativeTimerView',
                  layoutDirection: TextDirection.ltr,
                  creationParams: {},
                  creationParamsCodec: StandardMessageCodec(),
                ),
              ),
            ),
            const SizedBox(height: 20),
            Text(
              'Controls are fully native Android',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    fontStyle: FontStyle.italic,
                    color: Colors.grey,
                  ),
            ),
          ],
        ),
      ),
    );
  }
}
