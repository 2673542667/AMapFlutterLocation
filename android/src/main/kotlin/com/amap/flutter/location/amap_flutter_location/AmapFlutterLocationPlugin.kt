package com.amap.flutter.location.amap_flutter_location

import android.content.Context
import android.text.TextUtils
import com.amap.api.location.AMapLocationClient
import java.lang.reflect.Method
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** 高德地图定位sdkFlutterPlugin  */
class AMapFlutterLocationPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
  private var mContext: Context? = null

  private var locationClientMap: Map<String?, AMapLocationClientImpl>? =
    ConcurrentHashMap<String, AMapLocationClientImpl>(8)

  @Override
  fun onMethodCall(call: MethodCall, result: Result) {
    val callMethod: String = call.method
    when (call.method) {
      "updatePrivacyStatement" -> updatePrivacyStatement(call.arguments as Map)
      "setApiKey" -> setApiKey(call.arguments as Map)
      "setLocationOption" -> setLocationOption(call.arguments as Map)
      "startLocation" -> startLocation(call.arguments as Map)
      "stopLocation" -> stopLocation(call.arguments as Map)
      "destroy" -> destroy(call.arguments as Map)
      else -> result.notImplemented()
    }
  }

  @Override
  fun onListen(o: Object?, eventSink: EventChannel.EventSink?) {
    mEventSink = eventSink
  }

  @Override
  fun onCancel(o: Object?) {
    for (entry in locationClientMap.entrySet()) {
      entry.getValue().stopLocation()
    }
  }

  /**
   * 开始定位
   */
  private fun startLocation(argsMap: Map) {
    val locationClientImp: AMapLocationClientImpl? = getLocationClientImp(argsMap)
    if (null != locationClientImp) {
      locationClientImp.startLocation()
    }
  }


  /**
   * 停止定位
   */
  private fun stopLocation(argsMap: Map) {
    val locationClientImp: AMapLocationClientImpl? = getLocationClientImp(argsMap)
    if (null != locationClientImp) {
      locationClientImp.stopLocation()
    }
  }

  /**
   * 销毁
   *
   * @param argsMap
   */
  private fun destroy(argsMap: Map) {
    val locationClientImp: AMapLocationClientImpl? = getLocationClientImp(argsMap)
    if (null != locationClientImp) {
      locationClientImp.destroy()

      locationClientMap.remove(getPluginKeyFromArgs(argsMap))
    }
  }

  /**
   * 设置apikey
   *
   * @param apiKeyMap
   */
  private fun setApiKey(apiKeyMap: Map?) {
    if (null != apiKeyMap) {
      if (apiKeyMap.containsKey("android")
        && !TextUtils.isEmpty(apiKeyMap.get("android") as String)
      ) {
        AMapLocationClient.setApiKey(apiKeyMap.get("android") as String)
      }
    }
  }

  private fun updatePrivacyStatement(privacyShowMap: Map?) {
    if (null != privacyShowMap) {
      val locationClazz: Class<AMapLocationClient> = AMapLocationClient::class.java

      if (privacyShowMap.containsKey("hasContains") && privacyShowMap.containsKey("hasShow")) {
        val hasContains = privacyShowMap.get("hasContains") as Boolean
        val hasShow = privacyShowMap.get("hasShow") as Boolean
        try {
          val showMethod: Method = locationClazz.getMethod(
            "updatePrivacyShow",
            Context::class.java,
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType
          )

          showMethod.invoke(null, mContext, hasContains, hasShow)
        } catch (e: Throwable) {
//          e.printStackTrace();
        }
      }

      if (privacyShowMap.containsKey("hasAgree")) {
        val hasAgree = privacyShowMap.get("hasAgree") as Boolean
        try {
          val agreeMethod: Method = locationClazz.getMethod(
            "updatePrivacyAgree",
            Context::class.java,
            Boolean::class.javaPrimitiveType
          )
          agreeMethod.invoke(null, mContext, hasAgree)
        } catch (e: Throwable) {
//            e.printStackTrace();
        }
      }
    }
  }

  /**
   * 设置定位参数
   *
   * @param argsMap
   */
  private fun setLocationOption(argsMap: Map) {
    val locationClientImp: AMapLocationClientImpl? = getLocationClientImp(argsMap)
    if (null != locationClientImp) {
      locationClientImp.setLocationOption(argsMap)
    }
  }


  @Override
  fun onAttachedToEngine(binding: FlutterPluginBinding) {
    if (null == mContext) {
      mContext = binding.getApplicationContext()

      /**
       * 方法调用通道
       */
      val channel: MethodChannel =
        MethodChannel(binding.getBinaryMessenger(), CHANNEL_METHOD_LOCATION)
      channel.setMethodCallHandler(this)

      /**
       * 回调监听通道
       */
      val eventChannel: EventChannel =
        EventChannel(binding.getBinaryMessenger(), CHANNEL_STREAM_LOCATION)
      eventChannel.setStreamHandler(this)
    }
  }

  @Override
  fun onDetachedFromEngine(binding: FlutterPluginBinding?) {
    for (entry in locationClientMap.entrySet()) {
      entry.getValue().destroy()
    }
  }

  private fun getLocationClientImp(argsMap: Map): AMapLocationClientImpl? {
    if (null == locationClientMap) {
      locationClientMap = ConcurrentHashMap<String, AMapLocationClientImpl>(8)
    }

    val pluginKey = getPluginKeyFromArgs(argsMap)
    if (TextUtils.isEmpty(pluginKey)) {
      return null
    }

    if (!locationClientMap!!.containsKey(pluginKey)) {
      val locationClientImp: AMapLocationClientImpl =
        AMapLocationClientImpl(mContext, pluginKey, mEventSink)
      locationClientMap.put(pluginKey, locationClientImp)
    }
    return locationClientMap!![pluginKey]
  }

  private fun getPluginKeyFromArgs(argsMap: Map?): String? {
    var pluginKey: String? = null
    try {
      if (null != argsMap) {
        pluginKey = argsMap.get("pluginKey")
      }
    } catch (e: Throwable) {
      e.printStackTrace()
    }
    return pluginKey
  }

  companion object {
    private const val CHANNEL_METHOD_LOCATION = "amap_flutter_location"
    private const val CHANNEL_STREAM_LOCATION = "amap_flutter_location_stream"

    var mEventSink: EventChannel.EventSink? = null
  }
}
