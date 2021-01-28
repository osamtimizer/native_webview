package com.hisaichi5518.native_webview

import android.content.DialogInterface
import android.util.Log
import android.net.Uri
import android.provider.MediaStore
import android.os.Parcelable
import android.webkit.JsPromptResult
import android.content.Intent
import android.app.Activity
import android.webkit.JsResult
import android.webkit.MimeTypeMap
import android.webkit.WebChromeClient
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import io.flutter.plugin.common.MethodChannel


class NativeWebChromeClient(private val channel: MethodChannel) : WebChromeClient() {
    private val PICKER = 1
    val DEFAULT_MIME_TYPES = "image/*"
    override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>? , fileChooserParams: FileChooserParams?): Boolean {
        val acceptTypes: Array<String?> = fileChooserParams!!.getAcceptTypes()
        val allowMultiple = fileChooserParams!!.getMode() === WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE
        val intent: Intent = fileChooserParams!!.createIntent()
        return startPhotoPickerIntent(filePathCallback, intent, acceptTypes, allowMultiple)
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        channel.invokeMethod("onProgressChanged", mapOf(
            "progress" to newProgress
        ))
    }

    override fun onJsConfirm(view: WebView?, url: String?, message: String, result: JsResult): Boolean {
        channel.invokeMethod(
            "onJsConfirm",
            mapOf("message" to message),
            object : MethodChannel.Result {
                override fun notImplemented() {
                    Log.i("NativeWebChromeClient", "onJsConfirm is notImplemented")
                    result.cancel()
                }

                override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
                    Log.e("NativeWebChromeClient", "$errorCode $errorMessage $errorDetails")
                    result.cancel()
                }

                override fun success(response: Any?) {
                    var responseMessage: String? = null
                    var okLabel: String? = null
                    var cancelLabel: String? = null

                    val responseMap = response as? Map<String, Any>
                    if (responseMap != null) {
                        val handledByClient = responseMap["handledByClient"] as? Boolean
                        val action = responseMap["action"] as? Int
                        if (handledByClient != null && handledByClient) {
                            when (action) {
                                0 -> result.confirm()
                                1 -> result.cancel()
                                else -> result.cancel()
                            }
                            return
                        }

                        responseMessage = responseMap["message"] as? String ?: message
                        okLabel = responseMap["okLabel"] as? String
                        cancelLabel = responseMap["cancelLabel"] as? String
                    }

                    createConfirmDialog(
                        responseMessage ?: message,
                        result,
                        okLabel,
                        cancelLabel
                    )
                }
            })

        return true
    }

    override fun onJsAlert(view: WebView?, url: String?, message: String, result: JsResult): Boolean {
        channel.invokeMethod(
            "onJsAlert",
            mapOf("message" to message),
            object : MethodChannel.Result {
                override fun notImplemented() {
                    Log.i("NativeWebChromeClient", "onJsAlert is notImplemented")
                    result.cancel()
                }

                override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
                    Log.e("NativeWebChromeClient", "$errorCode $errorMessage $errorDetails")
                    result.cancel()
                }

                override fun success(response: Any?) {
                    var responseMessage: String? = null
                    var okLabel: String? = null

                    val responseMap = response as? Map<String, Any>
                    if (responseMap != null) {
                        val handledByClient = responseMap["handledByClient"] as? Boolean
                        if (handledByClient != null && handledByClient) {
                            result.confirm()
                            return
                        }

                        responseMessage = responseMap["message"] as? String ?: message
                        okLabel = responseMap["okLabel"] as? String
                    }

                    createAlertDialog(
                        responseMessage ?: message,
                        result,
                        okLabel
                    )
                }
            })

        return true
    }

    override fun onJsPrompt(view: WebView?, url: String?, message: String, defaultValue: String?, result: JsPromptResult): Boolean {
        channel.invokeMethod(
            "onJsPrompt",
            mapOf("message" to message),
            object : MethodChannel.Result {
                override fun notImplemented() {
                    Log.i("NativeWebChromeClient", "onJsPrompt is notImplemented")
                    result.cancel()
                }

                override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
                    Log.e("NativeWebChromeClient", "$errorCode $errorMessage $errorDetails")
                    result.cancel()
                }

                override fun success(response: Any?) {
                    var responseMessage: String? = null
                    var okLabel: String? = null
                    var cancelLabel: String? = null

                    val responseMap = response as? Map<String, Any>
                    if (responseMap != null) {
                        val handledByClient = responseMap["handledByClient"] as? Boolean
                        val action = responseMap["action"] as? Int
                        if (handledByClient != null && handledByClient) {
                            when (action) {
                                0 -> {
                                    val value = responseMap["value"] as? String
                                    result.confirm(value)
                                }
                                1 -> result.cancel()
                                else -> result.cancel()
                            }
                            return
                        }

                        responseMessage = responseMap["message"] as? String ?: message
                        okLabel = responseMap["okLabel"] as? String
                        cancelLabel = responseMap["cancelLabel"] as? String
                    }

                    createPromptDialog(
                        responseMessage ?: message,
                        defaultValue,
                        result,
                        okLabel,
                        cancelLabel
                    )
                }
            })
        return true
    }

    fun startPhotoPickerIntent(callback: ValueCallback<Array<Uri>>?, intent: Intent?, acceptTypes: Array<String?>?, allowMultiple: Boolean): Boolean {
        val activity: Activity = Locator.activity!!
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("image/*")
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, PICKER)
        } else {
            // do nothing
        }
        return true
    }

    private fun createAlertDialog(
        message: String,
        result: JsResult,
        okLabel: String?
    ) {
        val builder = AlertDialog.Builder(Locator.activity!!, R.style.Theme_AppCompat_Dialog_Alert).apply {
            setMessage(message)
        }
        val confirmClickListener = DialogInterface.OnClickListener { dialog, _ ->
            result.confirm()
            dialog.dismiss()
        }
        if (okLabel != null && okLabel.isNotEmpty()) {
            builder.setPositiveButton(okLabel, confirmClickListener)
        } else {
            builder.setPositiveButton(android.R.string.ok, confirmClickListener)
        }

        builder.setOnCancelListener { dialog ->
            result.cancel()
            dialog.dismiss()
        }
        builder.show()
    }

    private fun createConfirmDialog(
        message: String,
        result: JsResult,
        okLabel: String?,
        cancelLabel: String?
    ) {
        val builder = AlertDialog.Builder(Locator.activity!!, R.style.Theme_AppCompat_Dialog_Alert).apply {
            setMessage(message)
        }

        val confirmClickListener = DialogInterface.OnClickListener { dialog, _ ->
            result.confirm()
            dialog.dismiss()
        }
        if (okLabel != null && okLabel.isNotEmpty()) {
            builder.setPositiveButton(okLabel, confirmClickListener)
        } else {
            builder.setPositiveButton(android.R.string.ok, confirmClickListener)
        }

        val cancelClickListener = DialogInterface.OnClickListener { dialog, _ ->
            result.cancel()
            dialog.dismiss()
        }
        if (cancelLabel != null && cancelLabel.isNotEmpty()) {
            builder.setNegativeButton(cancelLabel, cancelClickListener)
        } else {
            builder.setNegativeButton(android.R.string.cancel, cancelClickListener)
        }
        builder.setOnCancelListener { dialog ->
            result.cancel()
            dialog.dismiss()
        }
        builder.show()
    }

    private fun createPromptDialog(
        message: String,
        defaultText: String?,
        result: JsPromptResult,
        okLabel: String?,
        cancelLabel: String?
    ) {
        val layout = FrameLayout(Locator.activity!!)
        val editText = EditText(Locator.activity!!).apply {
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setText(defaultText)
        }

        layout.setPaddingRelative(45, 15, 45, 0)
        layout.addView(editText)

        val builder = AlertDialog.Builder(Locator.activity!!, R.style.Theme_AppCompat_Dialog_Alert).apply {
            setMessage(message)
        }

        val confirmClickListener = DialogInterface.OnClickListener { dialog, _ ->
            result.confirm(editText.text.toString())
            dialog.dismiss()
        }
        if (okLabel != null && okLabel.isNotEmpty()) {
            builder.setPositiveButton(okLabel, confirmClickListener)
        } else {
            builder.setPositiveButton(android.R.string.ok, confirmClickListener)
        }

        val cancelClickListener = DialogInterface.OnClickListener { dialog, _ ->
            result.cancel()
            dialog.dismiss()
        }
        if (cancelLabel != null && cancelLabel.isNotEmpty()) {
            builder.setNegativeButton(cancelLabel, cancelClickListener)
        } else {
            builder.setNegativeButton(android.R.string.cancel, cancelClickListener)
        }

        builder.setView(layout)

        builder.setOnCancelListener { dialog ->
            result.cancel()
            dialog.dismiss()
        }
        builder.show()
    }

}