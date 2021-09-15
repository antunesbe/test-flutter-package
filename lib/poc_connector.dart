import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

class PocConnector {
  static const MethodChannel _channel = const MethodChannel('poc_connector');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List<dynamic>> get({
    required String url,
    Map<String, dynamic>? headers,
  }) async {
    var result = await _channel
        .invokeMethod('connectorGet', {'url': url, 'headers': headers});
    result = jsonDecode(result);
    return result['content'];
  }
}
