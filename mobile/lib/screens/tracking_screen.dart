import 'dart:async';

import 'package:flutter/material.dart';

import '../services/queue_alerts.dart';
import '../services/queue_api.dart';

class TrackingScreen extends StatefulWidget {
  const TrackingScreen({super.key, required this.api, required this.token});
  final QueueApi api;
  final String token;
  @override
  State<TrackingScreen> createState() => _TrackingScreenState();
}

class _TrackingScreenState extends State<TrackingScreen> {
  Timer? _timer;
  Ticket? _ticket;
  String? _error;
  bool _notified = false;

  @override
  void initState() {
    super.initState();
    _poll();
    _timer = Timer.periodic(const Duration(seconds: 3), (_) => _poll());
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _poll() async {
    try {
      final ticket = await widget.api.fetchTicket(widget.token);
      if (!mounted) return;
      setState(() {
        final wasCalled = _ticket?.isCalled ?? false;
        _ticket = ticket;
        _error = null;
        if (ticket.isCalled && !wasCalled && !_notified) {
          _notified = true;
          unawaited(queueAlerts.notifyYourTurn(ticket));
        } else if (!ticket.isCalled) {
          _notified = false;
        }
      });
    } catch (e) {
      if (!mounted) return;
      setState(() => _error = e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final ticket = _ticket;

    return Scaffold(
      appBar: AppBar(
        title: Text(ticket?.departmentName ?? 'Your ticket'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'Refresh now',
            onPressed: _poll,
          ),
          IconButton(
            icon: const Icon(Icons.close),
            tooltip: 'Back',
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
      body: SafeArea(
        child: ticket == null
            ? _buildLoading()
            : Column(
                children: [
                  if (_error != null)
                    Container(
                      width: double.infinity,
                      color: Colors.orange.shade100,
                      padding:
                          const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                      child: Text(
                        'Connection issue — retrying…',
                        style: TextStyle(color: Colors.orange.shade900, fontSize: 13),
                      ),
                    ),
                  Expanded(child: _buildContent(ticket, theme)),
                ],
              ),
      ),
    );
  }

  Widget _buildLoading() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const CircularProgressIndicator(),
          const SizedBox(height: 16),
          const Text('Connecting to queue server…'),
          if (_error != null) ...[
            const SizedBox(height: 12),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 32),
              child: Text(_error!,
                  textAlign: TextAlign.center,
                  style: TextStyle(color: Theme.of(context).colorScheme.error)),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildContent(Ticket t, ThemeData theme) {
    if (t.isCalled) return _CalledScreen(ticket: t);
    switch (t.status) {
      case 'In Consultation':
      case 'For Laboratory':
      case 'For Pharmacy':
        // Active at the hospital (consultation or sent for lab/pharmacy).
        return _StatusScreen(
          color: Colors.green,
          icon: Icons.check_circle,
          title: 'Being served',
          subtitle: t.counterName != null ? 'At ${t.counterName}' : null,
          ticket: t,
        );
      case 'Completed':
        return _StatusScreen(
          color: Colors.blueGrey,
          icon: Icons.done_all,
          title: 'Service completed',
          subtitle: 'Thank you! Rate your visit at the desk.',
          ticket: t,
        );
      case 'Discharged':
        return _StatusScreen(
          color: Colors.teal,
          icon: Icons.local_hospital,
          title: 'Discharged',
          subtitle: 'See you next time. Rate your visit at the desk.',
          ticket: t,
        );
      case 'No Show':
        return _StatusScreen(
          color: Colors.red,
          icon: Icons.event_busy,
          title: 'Marked as no-show',
          subtitle: 'Please contact the front desk.',
          ticket: t,
        );
      default:
        return _buildWaiting(t, theme);
    }
  }

  Widget _buildWaiting(Ticket t, ThemeData theme) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: SingleChildScrollView(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Waiting in line',
                style: TextStyle(fontSize: 20, color: Colors.grey.shade700)),
            const SizedBox(height: 4),
            Text(
              '#${t.queueNumber}',
              style: TextStyle(
                  fontSize: 84, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
            ),
            if (t.isSpecialPriority)
              Container(
                margin: const EdgeInsets.only(top: 4),
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.amber.shade100,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  '${t.priorityType} priority',
                  style: TextStyle(color: Colors.amber.shade900, fontWeight: FontWeight.w600),
                ),
              ),
            const SizedBox(height: 40),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _InfoCard(
                    label: 'Now serving',
                    value: t.currentNumber != null ? '#${t.currentNumber}' : '—'),
                _InfoCard(label: 'Patients ahead', value: '${t.patientsAhead ?? 0}'),
                _InfoCard(label: 'Est. wait', value: '${t.estimatedWaitMinutes ?? 0} min'),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _InfoCard(label: 'Service', value: t.serviceName ?? '—'),
                _InfoCard(label: 'Counter', value: t.counterName ?? '—'),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  const _InfoCard({required this.label, required this.value});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 108,
      margin: const EdgeInsets.symmetric(horizontal: 6),
      padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 8),
      decoration: BoxDecoration(
        color: Theme.of(context).cardColor,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Column(
        children: [
          Text(value,
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 4),
          Text(label,
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 11, color: Colors.grey.shade600)),
        ],
      ),
    );
  }
}

class _CalledScreen extends StatelessWidget {
  const _CalledScreen({required this.ticket});
  final Ticket ticket;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.red.shade600,
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.notifications_active, size: 96, color: Colors.white),
              const SizedBox(height: 24),
              const Text(
                "IT'S YOUR TURN!",
                textAlign: TextAlign.center,
                style:
                    TextStyle(fontSize: 40, fontWeight: FontWeight.bold, color: Colors.white),
              ),
              const SizedBox(height: 16),
              Text(
                'Proceed to ${ticket.counterName ?? 'the counter'}\nTicket #${ticket.queueNumber}',
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 22, color: Colors.white70),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _StatusScreen extends StatelessWidget {
  const _StatusScreen({
    required this.color,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.ticket,
  });
  final Color color;
  final IconData icon;
  final String title;
  final String? subtitle;
  final Ticket ticket;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 88, color: color),
            const SizedBox(height: 16),
            Text(title,
                style: TextStyle(
                    fontSize: 28, fontWeight: FontWeight.bold, color: color)),
            if (subtitle != null) ...[
              const SizedBox(height: 8),
              Text(subtitle!,
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 16, color: Colors.grey.shade700)),
            ],
            const SizedBox(height: 24),
            Text('Ticket #${ticket.queueNumber} · ${ticket.serviceName ?? ''}',
                style: TextStyle(fontSize: 14, color: Colors.grey.shade500)),
          ],
        ),
      ),
    );
  }
}
