import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_queue_app/main.dart';

void main() {
  testWidgets('App renders the home screen', (tester) async {
    await tester.pumpWidget(const QueueApp());
    expect(find.text('Hospital Queue — Patient'), findsOneWidget);
    expect(find.text('Scan QR code'), findsOneWidget);
    expect(find.text('Enter ticket code'), findsOneWidget);
  });
}
