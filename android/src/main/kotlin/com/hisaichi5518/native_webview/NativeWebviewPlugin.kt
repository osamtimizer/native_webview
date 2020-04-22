package com.hisaichi5518.native_webview

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.platform.PlatformViewRegistry
import io.flutter.plugin.common.PluginRegistry

class NativeWebviewPlugin : FlutterPlugin, ActivityAware {
    private var cookieManager: MyCookieManager? = null

    companion object {
        public fun registerWith(registrar: PluginRegistry.Registrar) {
            Locator.activity = registrar.activity()
            Locator.registrar = registrar // v1

            val instance = NativeWebviewPlugin()
            instance.onAttachedToEngine(
                registrar.messenger(),
                registrar.platformViewRegistry()
            )
        }
    }

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Locator.binding = binding

        onAttachedToEngine(
            binding.binaryMessenger,
            binding.platformViewRegistry
        )
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        Locator.binding = null
    }

    fun onAttachedToEngine(messenger: BinaryMessenger, registry: PlatformViewRegistry) {
        registry.registerViewFactory(
            "com.hisaichi5518/native_webview",
            FlutterWebViewFactory(messenger)
        )
        cookieManager = MyCookieManager(messenger)
    }

    override fun onDetachedFromActivity() {
        Locator.activity = null
        cookieManager?.dispose()
        cookieManager = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Locator.activity = binding.activity
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Locator.activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Locator.activity = null
    }
}
