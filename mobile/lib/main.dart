import 'package:flutter/material.dart';

import 'screens/home_screen.dart';
import 'services/queue_alerts.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await queueAlerts.init();
  runApp(const QueueApp());
}

class QueueApp extends StatelessWidget {
  const QueueApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Hospital Queue',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorSchemeSeed: const Color(0xFF0D47A1),
        useMaterial3: true,
      ),
      home: const HomeScreen(),
    );
  }
}
