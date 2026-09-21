import 'package:flutter/material.dart';

import '../services/queue_api.dart';
import 'scanner_screen.dart';
import 'tracking_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _hostController = TextEditingController(text: '10.0.2.2');
  String? _serverStatus;
  bool _checking = false;

  QueueApi get _api => QueueApi.fromHost(_hostController.text);

  @override
  void dispose() {
    _hostController.dispose();
    super.dispose();
  }

  Future<void> _checkServer() async {
    setState(() {
      _checking = true;
      _serverStatus = null;
    });
    final ok = await _api.ping();
    if (!mounted) return;
    setState(() {
      _checking = false;
      _serverStatus = ok ? 'Connected to ${_api.baseUrl}' : 'Cannot reach ${_api.baseUrl}';
    });
  }

  Future<void> _track(String? token) async {
    final effective = token ?? await _promptForToken();
    if (effective == null || effective.isEmpty || !mounted) return;
    await Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => TrackingScreen(api: _api, token: effective)),
    );
  }

  Future<String?> _promptForToken() {
    final controller = TextEditingController();
    return showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Ticket code'),
        content: TextField(
          controller: controller,
          autofocus: true,
          textInputAction: TextInputAction.done,
          decoration: const InputDecoration(
            hintText: 'Type the code shown on your ticket',
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () =>
                Navigator.pop(context, controller.text.trim().isEmpty ? null : controller.text.trim()),
            child: const Text('Track'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final connected = _serverStatus?.startsWith('Connected') ?? false;
    return Scaffold(
      appBar: AppBar(title: const Text('Hospital Queue — Patient')),
      body: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          const Text('Server', style: TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _hostController,
                  keyboardType: TextInputType.url,
                  autocorrect: false,
                  decoration: const InputDecoration(
                    hintText: '10.0.2.2 (emulator) or PC LAN IP (real phone)',
                    border: OutlineInputBorder(),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              IconButton.filledTonal(
                onPressed: _checking ? null : _checkServer,
                icon: _checking
                    ? const SizedBox(
                        width: 18,
                        height: 18,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.wifi),
                tooltip: 'Test connection',
              ),
            ],
          ),
          if (_serverStatus != null)
            Padding(
              padding: const EdgeInsets.only(top: 8),
              child: Text(
                _serverStatus!,
                style: TextStyle(color: connected ? Colors.green : Colors.redAccent),
              ),
            ),
          const SizedBox(height: 32),
          const Text('Your ticket', style: TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          _OptionTile(
            icon: Icons.qr_code_scanner,
            title: 'Scan QR code',
            subtitle: 'Scan the code shown at registration',
            onTap: () async {
              final token = await Navigator.of(context).push<String>(
                MaterialPageRoute(builder: (_) => const ScannerScreen()),
              );
              await _track(token);
            },
          ),
          const SizedBox(height: 12),
          _OptionTile(
            icon: Icons.edit_note,
            title: 'Enter ticket code',
            subtitle: 'Type the code shown on your ticket',
            onTap: () => _track(null),
          ),
        ],
      ),
    );
  }
}

class _OptionTile extends StatelessWidget {
  const _OptionTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              CircleAvatar(radius: 24, child: Icon(icon, size: 26)),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title,
                        style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
                    const SizedBox(height: 2),
                    Text(subtitle, style: TextStyle(fontSize: 13, color: Colors.grey.shade600)),
                  ],
                ),
              ),
              Icon(Icons.chevron_right, color: Colors.grey.shade500),
            ],
          ),
        ),
      ),
    );
  }
}
