import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:unique_device_id/unique_device_id.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await UniqueDeviceId.instance.setDefaultUseUUID(true);

  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String? uniqueId;

  @override
  void initState() {
    super.initState();

    getUniqueId().then(
      (value) => setState(
        () => uniqueId = value,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center( 
          child: uniqueId?.isNotEmpty ?? false
              ? Text('Unique ID: $uniqueId\n')
              : CircularProgressIndicator(),
        ),
      ),
    );
  }

  Future<String?> getUniqueId() async {
    try {
      return await UniqueDeviceId.instance.getUniqueId();
    } on PlatformException catch (e) {
      if (e.code == '1011') {
        final status = await Permission.storage.request();
        if (status.isGranted) {
          return getUniqueId();
        } else if (status.isPermanentlyDenied) {
          openAppSettings();
        }
      }
      return null;
    }
  }
}
