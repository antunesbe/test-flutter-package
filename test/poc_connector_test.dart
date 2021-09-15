import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:poc_connector/poc_connector.dart';

void main() {
  const MethodChannel channel = MethodChannel('poc_connector');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await PocConnector.platformVersion, '42');
  });
}
