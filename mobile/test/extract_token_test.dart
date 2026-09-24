import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_queue_app/services/queue_api.dart';

void main() {
  group('extractToken', () {
    test('bare token passes through', () {
      expect(extractToken('T-ME'), 'T-ME');
      expect(extractToken('7f3a9b2c'), '7f3a9b2c');
      expect(extractToken('  T-EMG  '), 'T-EMG');
    });

    test('full API URL returns last path segment', () {
      expect(
        extractToken('http://192.168.1.42:5000/api/ticket/T-ME'),
        'T-ME',
      );
      expect(
        extractToken('http://10.0.2.2:5000/api/ticket/7f3a9b2c'),
        '7f3a9b2c',
      );
    });

    test('HOSP payload returns token part', () {
      expect(extractToken('HOSP:Q:T-ME'), 'T-ME');
      expect(extractToken('HOSP:Q:7f3a9b2c'), '7f3a9b2c');
    });

    test('URL without /api/ticket/ uses last path segment', () {
      expect(extractToken('https://example.com/about'), 'about');
    });

    test('URL with empty path falls back to raw value', () {
      expect(extractToken('https://example.com'), 'https://example.com');
    });
  });
}
