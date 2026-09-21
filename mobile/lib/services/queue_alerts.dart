import 'package:flutter_local_notifications/flutter_local_notifications.dart';

import 'queue_api.dart';

final queueAlerts = QueueAlerts();

class QueueAlerts {
  static const channelId = 'queue_alerts';
  late final FlutterLocalNotificationsPlugin _plugin;
  bool _ready = false;

  Future<void> init() async {
    _plugin = FlutterLocalNotificationsPlugin();
    await _plugin.initialize(
      settings: const InitializationSettings(
        android: AndroidInitializationSettings('@mipmap/ic_launcher'),
      ),
    );
    final android = _plugin
        .resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
    await android?.createNotificationChannel(
      const AndroidNotificationChannel(
        channelId,
        'Queue alerts',
        description: 'Ticket called / status updates',
        importance: Importance.max,
      ),
    );
    await android?.requestNotificationsPermission();
    _ready = true;
  }

  Future<void> notifyYourTurn(Ticket ticket) async {
    if (!_ready) return;
    await _plugin.show(
      id: 1,
      title: "It's your turn!",
      body: 'Proceed to ${ticket.counterName ?? 'your counter'} — ticket #${ticket.queueNumber}',
      notificationDetails: const NotificationDetails(
        android: AndroidNotificationDetails(
          channelId,
          'Queue alerts',
          importance: Importance.max,
          priority: Priority.high,
          playSound: true,
          enableVibration: true,
        ),
      ),
    );
  }
}
