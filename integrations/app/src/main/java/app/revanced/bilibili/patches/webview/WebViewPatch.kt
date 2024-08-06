package app.revanced.bilibili.patches.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebView.VisualStateCallback
import android.webkit.WebViewClient
import androidx.annotation.Keep
import app.revanced.bilibili.settings.Settings
import app.revanced.bilibili.utils.Versions
import app.revanced.bilibili.utils.saveImage as saveImageReal

object WebViewPatch {
    private val jsHooker = object : Any() {
        @Suppress("UNUSED")
        @JavascriptInterface
        fun saveImage(url: String) {
            saveImageReal(url.substringBeforeLast('@'))
        }
    }

    @Keep
    @JvmStatic
    fun setWebViewClient(webView: WebView, client: WebViewClient) {
        webView.addJavascriptInterface(jsHooker, "hooker")
        webView.webViewClient = WebViewClientProxy(client, object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (!Settings.SaveCommentImage() && !Versions.ge7_76_0()) return
                if (url.startsWith("https://www.bilibili.com/h5/note-app/view")) {
                    view.postVisualStateCallback(0, object : VisualStateCallback() {
                        override fun onComplete(requestId: Long) {
                            view.evaluateJavascript(
                                """(function(){for(var i=0;i<document.images.length;++i){var image=document.images[i];if(image.className==='img-preview'||image.parentElement.className==='img-preview'){image.addEventListener("contextmenu",(e)=>{hooker.saveImage(e.target.currentSrc)})}}})()""",
                                null
                            )
                        }
                    })
                }
            }
        })
    }
}
