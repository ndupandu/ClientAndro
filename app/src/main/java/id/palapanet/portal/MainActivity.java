package id.palapanet.portal;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

/** Pembungkus WebView untuk portal pelanggan. Semua isi (tagihan, bayar) tetap dari server. */
public class MainActivity extends Activity {
    private WebView web;
    private ProgressBar bar;

    private static final String OFFLINE_HTML =
        "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'></head>"
        + "<body style='font-family:sans-serif;text-align:center;padding:48px 24px;color:#0a2733;background:#eaf2f5'>"
        + "<h2>Tidak dapat terhubung</h2><p>Periksa koneksi internet Anda, lalu coba lagi.</p>"
        + "<p><a href='" + BuildConfig.PORTAL_URL + "' style='display:inline-block;padding:12px 28px;border-radius:12px;"
        + "background:#0a84b5;color:#fff;text-decoration:none;font-weight:bold'>Coba lagi</a></p></body></html>";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout root = new FrameLayout(this);
        web = new WebView(this);
        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, (int) (3 * getResources().getDisplayMetrics().density));
        root.addView(web, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(bar, lp);
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);          // portal memakai JavaScript
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setSupportMultipleWindows(false);
        CookieManager.getInstance().setAcceptCookie(true);   // sesi login pelanggan disimpan di cookie

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String sc = u.getScheme() == null ? "" : u.getScheme();
                if (sc.equals("https")) return false;            // portal & halaman pembayaran Midtrans tetap di dalam aplikasi
                if (sc.equals("http")) return true;              // tolak koneksi tidak aman
                try {                                            // gopay://, shopeeid://, tel:, dll. -> buka aplikasi terkait
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (ActivityNotFoundException ignored) { }
                return true;
            }

            @Override
            public void onPageStarted(WebView v, String url, Bitmap f) { bar.setVisibility(View.VISIBLE); }

            @Override
            public void onPageFinished(WebView v, String url) {
                bar.setVisibility(View.GONE);
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) v.loadDataWithBaseURL(BuildConfig.PORTAL_URL, OFFLINE_HTML, "text/html", "utf-8", null);
            }
        });
        web.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public void onProgressChanged(WebView v, int p) { bar.setProgress(p); }
        });

        if (state != null) web.restoreState(state);
        else web.loadUrl(BuildConfig.PORTAL_URL);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
