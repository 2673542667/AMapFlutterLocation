import 'package:flutter_test/flutter_test.dart';
import 'package:amap_flutter_location/amap_flutter_location.dart';
import 'package:amap_flutter_location/amap_flutter_location_platform_interface.dart';
import 'package:amap_flutter_location/amap_flutter_location_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockAmapFlutterLocationPlatform
    with MockPlatformInterfaceMixin
    implements AmapFlutterLocationPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final AmapFlutterLocationPlatform initialPlatform = AmapFlutterLocationPlatform.instance;

  test('$MethodChannelAmapFlutterLocation is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelAmapFlutterLocation>());
  });

  test('getPlatformVersion', () async {
    AmapFlutterLocation amapFlutterLocationPlugin = AmapFlutterLocation();
    MockAmapFlutterLocationPlatform fakePlatform = MockAmapFlutterLocationPlatform();
    AmapFlutterLocationPlatform.instance = fakePlatform;

    expect(await amapFlutterLocationPlugin.getPlatformVersion(), '42');
  });
}
