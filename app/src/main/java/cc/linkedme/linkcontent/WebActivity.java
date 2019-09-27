package cc.linkedme.linkcontent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cc.linkedme.linkcontent.linkcontentutils.LMContentUtils;
import cc.linkedme.linkcontent.linkcontentutils.LMWebChromeClient;
import cc.linkedme.linkcontent.linkcontentutils.LMWebViewClient;
import cc.linkedme.linkcontent.linkcontentutils.OnLoadUrlListener;
import cc.linkedme.linkcontent.linkcontentutils.OnReceivedTitleListener;

public class WebActivity extends AppCompatActivity implements OnLoadUrlListener, OnReceivedTitleListener {

    private static final String TAG = "linkcontent-webactivity";
    private ImageView back;
    private String imei;
    private ConstraintLayout toolbar;
    private FrameLayout container;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        back = findViewById(R.id.back);
        toolbar = findViewById(R.id.toolbar);
        container = findViewById(R.id.container);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1001);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        imei = LMContentUtils.getDeviceId(this);
        String loadUrl = "http://content.linkedme.cc/feed/index.html?app_key=7e289a2484f4368dbafbd1e5c7d06903&device_type=1";
        if (!TextUtils.isEmpty(imei)) {
            loadUrl += "&device_id=" + imei;
        }
        loadUrl += "&nt=" + LMContentUtils.getNetworkState(this);
        addWebView(loadUrl, false);

    }

    private void addWebView(String url, boolean loadUrlInSelf) {
        WebView webview = new WebView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        webview.setLayoutParams(params);
        WebSettings webSettings = webview.getSettings();
        webview.setWebChromeClient(new LMWebChromeClient(WebActivity.this));
        webview.setWebViewClient(new LMWebViewClient(WebActivity.this, loadUrlInSelf));
        webSettings.setJavaScriptEnabled(true);
        // 必需开启开权限，否则无法上报广告数据，影响结算
        webSettings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webview.loadUrl(url);
        container.addView(webview);
    }


    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        int childCount = container.getChildCount();
        // 取最新的WebView
        WebView latestWebView = (WebView) container.getChildAt(childCount - 1);
        if (latestWebView.canGoBack()) {
            latestWebView.goBack();
        } else {
            if (childCount == 2) {
                // 返回信息流列表页面隐藏toolbar
                toolbar.setVisibility(View.GONE);
            }
            if (childCount > 1) {
                container.removeViewAt(childCount - 1);
            } else {
                super.onBackPressed();
            }
        }

    }

    @Override
    public void onReceivedTitle(String title) {
        if (title != null) {
            ((TextView) findViewById(R.id.title)).setText(title);
        }
    }

    @Override
    public void onLoadUrl(String url) {
        toolbar.setVisibility(View.VISIBLE);
        addWebView(url, true);
    }
}
