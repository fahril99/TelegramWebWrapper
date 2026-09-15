package com.example.telegramweb

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: LinearLayout
    private lateinit var btnRetry: Button
    
    private val telegramUrl = "https://web.telegram.org/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        errorLayout = findViewById(R.id.errorLayout)
        btnRetry = findViewById(R.id.btnRetry)

        setupWebView()

        btnRetry.setOnClickListener {
            errorLayout.visibility = View.GONE
            webView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            webView.reload()
        }

        if (savedInstanceState == null) {
            webView.loadUrl(telegramUrl)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Enable Cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                injectHideSearchBarScript(view)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    showError()
                }
            }
        }
    }

    private fun injectHideSearchBarScript(view: WebView?) {
        val jsScript = """
            (function() {
                if (window.searchBarHiddenInjected) return;
                window.searchBarHiddenInjected = true;

                var style = document.createElement('style');
                style.innerHTML = `
                    input[placeholder*="Search" i],
                    input[aria-label*="Search" i],
                    .SearchInput,
                    #telegram-search-input,
                    .search-input,
                    .input-search,
                    .sidebar-header .search-container,
                    .sidebar-header .chat-search,
                    .search-group,
                    .search-super-group {
                        display: none !important;
                        visibility: hidden !important;
                        opacity: 0 !important;
                        pointer-events: none !important;
                        height: 0 !important;
                        width: 0 !important;
                        position: absolute !important;
                    }
                `;
                document.head.appendChild(style);

                var observer = new MutationObserver(function(mutations) {
                    var searchInputs = document.querySelectorAll('input');
                    searchInputs.forEach(function(input) {
                        var ph = (input.getAttribute('placeholder') || '').toLowerCase();
                        var aria = (input.getAttribute('aria-label') || '').toLowerCase();
                        
                        if (ph.includes('search') || aria.includes('search') || ph.includes('cari') || aria.includes('cari')) {
                            input.style.display = 'none';
                            
                            var wrapper = input;
                            for (var i = 0; i < 3; i++) {
                                if (wrapper.parentElement && wrapper.parentElement.tagName !== 'BODY') {
                                    wrapper = wrapper.parentElement;
                                    var classText = wrapper.className;
                                    if (typeof classText === 'string' && (classText.toLowerCase().includes('search') || classText.toLowerCase().includes('input'))) {
                                        wrapper.style.display = 'none';
                                        break;
                                    }
                                }
                            }
                        }
                    });
                });
                observer.observe(document.body, { childList: true, subtree: true });
            })();
        """.trimIndent()
        
        view?.evaluateJavascript(jsScript, null)
    }

    private fun showError() {
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }
}
