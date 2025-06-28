import 'dart:async';

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
  late Timer _timer;
  int _duration = 0;
  bool _isRunning = false;

  @override
  void initState() {
    super.initState();
    platform.setMethodCallHandler(_handleMethodCall);
  }

  Future<void> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'start':
        _startTimer();
        break;
      case 'pause':
        _pauseTimer();
        break;
      case 'reset':
        _resetTimer();
        break;
      default:
        throw MissingPluginException();
    }
  }

  void _startTimer() {
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      setState(() {
        _duration++;
      });
    });
    setState(() {
      _isRunning = true;
    });
  }

  void _pauseTimer() {
    _timer.cancel();
    setState(() {
      _isRunning = false;
    });
  }

  void _resetTimer() {
    _timer.cancel();
    setState(() {
      _duration = 0;
      _isRunning = false;
    });
  }

  String _formatDuration(int seconds) {
    final duration = Duration(seconds: seconds);
    String twoDigits(int n) => n.toString().padLeft(2, '0');
    String twoDigitMinutes = twoDigits(duration.inMinutes.remainder(60));
    String twoDigitSeconds = twoDigits(duration.inSeconds.remainder(60));
    return "${twoDigits(duration.inHours)}:$twoDigitMinutes:$twoDigitSeconds";
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
              _formatDuration(_duration),
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 20),
            SizedBox(
              height: 100,
              child: AndroidView(
                viewType: 'NativeTimerView',
                layoutDirection: TextDirection.ltr,
                creationParams: const {},
                creationParamsCodec: const StandardMessageCodec(),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
