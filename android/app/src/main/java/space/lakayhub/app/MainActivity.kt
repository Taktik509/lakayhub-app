package space.lakayhub.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * LakayHub — Aplikasyon natif Android
 * WebView ak sekirite wo nivo + senkronizasyon otomatik ak sit la
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    companion object {
        const val BASE_URL = "https://lakayhub.space"
        const val APP_VERSION = 1
        const val VERSION_URL = "https://lakayhub.space/app/version.json"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        swipeRefresh = findViewById(R.id.swipe_refresh)

        // ---------- SEKIRITE WO NIVO ----------
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // BLOKE: aksè fichye lokal (anpeche atak)
            allowFileAccess = false
            allowContentAccess = false
            // HTTPS sèlman
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            // Pa sove modpas nan WebView
            savePassword = false
            // User agent koutim pou idantifye app lan
            userAgentString = "$userAgentString LakayHub-Android/$APP_VERSION"
        }

        // Pa kach done sansib
        webView.clearCache(false)

        webView.webViewClient = object : WebViewClient() {
            // BLOKE navigasyon deyò lakayhub.space (anti-phishing)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                return if (url.startsWith(BASE_URL)) {
                    false // Rete nan app lan
                } else {
                    // Louvri lyen ekstèn nan navigatè a
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: android.net.http.SslError?) {
                // BLOKE tout erè SSL — pa kontinye (sekirite)
                handler?.cancel()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
            }
        }

        // Deep link: louvri paj espesifik (peye, swiv, elatriye)
        val page = intent.getStringExtra("page")
        val startUrl = when (page) {
            "peye" -> "$BASE_URL/lakaypay/peye/?app=1"
            "swiv" -> "$BASE_URL/lakaypay/swiv/?app=1"
            "patne" -> "$BASE_URL/lakaypay/patne/?app=1"
            else -> "$BASE_URL/?app=1"
        }

        // Chaje sit la — SENKRONIZASYON OTOMATIK (toujou dènye vèsyon)
        webView.loadUrl(startUrl)

        // Pull-to-refresh
        swipeRefresh.setOnRefreshListener { webView.reload() }

        // Tcheke mizajou vèsyon app lan
        checkAppUpdate()
    }

    /**
     * Tcheke si gen nouvo vèsyon APK disponib sou sèvè a
     * (paske pa sou Play Store — distribisyon dirèk)
     */
    private fun checkAppUpdate() {
        Thread {
            try {
                val url = java.net.URL(VERSION_URL)
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 10000
                val json = conn.inputStream.bufferedReader().readText()
                // Fòma: {"version": 2, "url": "https://lakayhub.space/app/lakayhub-v2.apk"}
                val latest = Regex("\"version\"\\s*:\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toInt() ?: APP_VERSION
                if (latest > APP_VERSION) {
                    val apkUrl = Regex("\"url\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
                    runOnUiThread { showUpdateDialog(apkUrl) }
                }
            } catch (e: Exception) { /* Pa gen koneksyon — inyore */ }
        }.start()
    }

    private fun showUpdateDialog(apkUrl: String?) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Mizajou disponib")
            .setMessage("Yon nouvo vèsyon LakayHub disponib. Vle telechaje l?")
            .setPositiveButton("Wi") { _, _ ->
                apkUrl?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
            }
            .setNegativeButton("Pita", null)
            .show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
