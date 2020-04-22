package com.hisaichi5518.native_webview

import android.app.Activity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.PluginRegistry

class Locator {
    companion object {
        var activity: Activity? = null
        var registrar: PluginRegistry.Registrar? = null // v1
        var binding: FlutterPlugin.FlutterPluginBinding? = null // v2
    }
}