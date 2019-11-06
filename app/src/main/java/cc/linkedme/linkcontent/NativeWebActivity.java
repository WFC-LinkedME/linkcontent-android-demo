package cc.linkedme.linkcontent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;

import cc.linkedme.linkcontent.linkcontentutils.LMContentUtils;
import cc.linkedme.linkcontent.linkcontentutils.LMWebChromeClient;
import cc.linkedme.linkcontent.linkcontentutils.LMWebViewClient;

public class NativeWebActivity extends AppCompatActivity {

    private String imei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_web);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1001);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        imei = LMContentUtils.getDeviceId(this);
        String loadUrl = "http://10.11.11.188:8080/h5/index.html?app_key=7e289a2484f4368dbafbd1e5c7d06903&device_type=1";
        if (!TextUtils.isEmpty(imei)) {
            loadUrl += "&device_id=" + imei;
        }
        loadUrl += "&nt=" + LMContentUtils.getNetworkState(this);
        addWebView("https://www.baidu.com", true);
    }


    private void addWebView(String url, boolean loadUrlInSelf) {
        WebView webview = findViewById(R.id.webview);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT
//        );
//        webview.setLayoutParams(params);
        WebSettings webSettings = webview.getSettings();
        webview.setWebChromeClient(new LMWebChromeClient(NativeWebActivity.this));
        webview.setWebViewClient(new LMWebViewClient(NativeWebActivity.this, loadUrlInSelf));
        webSettings.setJavaScriptEnabled(true);
        // 必需开启开权限，否则无法上报广告数据，影响结算
        webSettings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webview.loadUrl(url);
    }


}
