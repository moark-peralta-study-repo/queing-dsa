import 'dart:convert';

import 'package:http/http.dart' as http;

class TicketException implements Exception {
  final String message;
  TicketException(this.message);
  @override
  String toString() => message;
}

class Ticket {
  final String token;
  final int queueId;
  final int queueNumber;
  final String? departmentName;
  final String? serviceName;
  final String status;
  final String? priorityType;
  final int? currentNumber;
  final int? patientsAhead;
  final int? estimatedWaitMinutes;
  final String? counterName;
  final String? joinedAt;
  final String? calledAt;

  Ticket({
    required this.token,
    required this.queueId,
    required this.queueNumber,
    required this.status,
    this.departmentName,
    this.serviceName,
    this.priorityType,
    this.currentNumber,
    this.patientsAhead,
    this.estimatedWaitMinutes,
    this.counterName,
    this.joinedAt,
    this.calledAt,
  });

  factory Ticket.fromJson(Map<String, dynamic> json) {
    return Ticket(
      token: json['token'] as String? ?? '',
      queueId: (json['queueId'] as num?)?.toInt() ?? 0,
      queueNumber: (json['queueNumber'] as num?)?.toInt() ?? 0,
      status: json['status'] as String? ?? 'Waiting',
      departmentName: json['departmentName'] as String?,
      serviceName: json['serviceName'] as String?,
      priorityType: json['priorityType'] as String?,
      currentNumber: (json['currentNumber'] as num?)?.toInt(),
      patientsAhead: (json['patientsAhead'] as num?)?.toInt(),
      estimatedWaitMinutes: (json['estimatedWaitMinutes'] as num?)?.toInt(),
      counterName: json['counterName'] as String?,
      joinedAt: json['joinedAt'] as String?,
      calledAt: json['calledAt'] as String?,
    );
  }

  /// "It's your turn" — the ticket was just called to the counter.
  bool get isCalled => status == 'Checked In';

  /// Currently being served at a counter.
  bool get isInService => status == 'In Consultation';

  bool get isSpecialPriority => priorityType != null && priorityType != 'REGULAR';
}

/// Pulls a ticket token out of whatever the QR code contains:
/// a bare token, a `HOSP:Q:<token>` payload, or a full
/// `http://host:port/api/ticket/<token>` URL.
String extractToken(String raw) {
  final trimmed = raw.trim();
  if (trimmed.contains('/api/ticket/')) {
    return trimmed.split('/api/ticket/').last.trim();
  }
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
    try {
      final segments = Uri.parse(trimmed).pathSegments;
      if (segments.isNotEmpty) return segments.last;
    } catch (_) {}
    return trimmed;
  }
  if (trimmed.startsWith('HOSP:')) {
    final parts = trimmed.split(':');
    if (parts.length >= 3) return parts.sublist(2).join(':');
  }
  return trimmed;
}

class QueueApi {
  final String baseUrl;

  QueueApi(this.baseUrl);

  factory QueueApi.fromHost(String host) {
    var h = host.trim().toLowerCase();
    if (h.startsWith('http://')) {
      h = h.substring('http://'.length);
    }
    if (!h.contains(':')) {
      h = '$h:5000';
    }
    return QueueApi('http://$h');
  }

  Future<bool> ping() async {
    try {
      final res = await http
          .get(Uri.parse('$baseUrl/api/health'))
          .timeout(const Duration(seconds: 4));
      return res.statusCode == 200;
    } catch (_) {
      return false;
    }
  }

  Future<Ticket> fetchTicket(String token) async {
    final res = await http
        .get(Uri.parse('$baseUrl/api/ticket/$token'))
        .timeout(const Duration(seconds: 4));
    if (res.statusCode == 404) {
      throw TicketException('Ticket not found on the server.');
    }
    if (res.statusCode != 200) {
      throw TicketException('Server error (HTTP ${res.statusCode}).');
    }
    return Ticket.fromJson(jsonDecode(res.body) as Map<String, dynamic>);
  }
}
