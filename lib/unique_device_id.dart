import 'dart:io';

import 'package:flutter/services.dart';

class UniqueDeviceId {
  static const _channel = const MethodChannel('unique_device_id');

  static final _instance = UniqueDeviceId._();

  static UniqueDeviceId get instance => _instance;

  UniqueDeviceId._();

  ///
  /// Set secret key for Only Android
  ///
  /// Key is over 16 digits
  ///
  Future<void> setSecretKey(String key) async {
    if (!Platform.isAndroid) return;
    if (key.length < 16) return;
    return _channel.invokeMethod('setSecretKey', key);
  }

  ///
  /// Use default UUID
  ///
  Future<void> setDefaultUseUUID(bool use) => _channel.invokeMethod('setDefaultUseUUID', use);

  ///
  /// Get unique id
  ///
  /// - Android: SSAID, fallback saved random UUID
  /// - iOS: identifierForVendor
  Future<String?> getUniqueId() => _channel.invokeMethod('getUniqueId');
}
