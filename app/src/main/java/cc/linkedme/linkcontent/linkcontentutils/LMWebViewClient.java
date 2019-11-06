package cc.linkedme.linkcontent.linkcontentutils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LMWebViewClient extends WebViewClient {

    private static final String TAG = "link_content";

    private Context context;
    // 是否在已有的 webview 中加载链接，true: 在已有的 webview 中加载 false: 重新创建新的 webview 加载链接
    private boolean loadUrlInSelf;

    public LMWebViewClient(Context context, boolean loadUrlInSelf) {
        this.context = context;
        this.loadUrlInSelf = loadUrlInSelf;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //去掉回车、换行、tab
        String stray_spacing = "[\n\r\t\\p{Zl}\\p{Zp}\u0085]+";
        url = url.trim();
        url = url.replaceAll(stray_spacing, "");
        String rfc2396regex = "^(([a-zA-Z][a-zA-Z0-9\\+\\-\\.]*)://)(([^/?#]*)?([^?#]*)(\\?([^#]*))?)?(#(.*))?";
        String all_schemes_pattern = "(?i)^(http|https|ftp|mms|rtsp|wais)://.*";
        if (url.matches(all_schemes_pattern)) {
            if (url.contains(".apk")) {
                Toast.makeText(context, "检测到 apk 文件", Toast.LENGTH_SHORT).show();
            } else {
                if (loadUrlInSelf) {
                    view.loadUrl(url);
                    return false;
                } else {
                    ((OnLoadUrlListener) context).onLoadUrl(url);
                    return true;
                }
            }
        }
        if (url.matches(rfc2396regex)) {
            openApp(url);
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.i(TAG, "onPageFinished: 页面加载完成");
        super.onPageFinished(view, url);

        if (url.equals(view.getOriginalUrl())) {
            Log.i(TAG, "onPageFinished: url =" + url + ", view.getOriginalUrl()=" + view.getOriginalUrl());
            injectLinkedMEADHelper(view);
        }

    }

    // 注入js函数监听
    public void injectLinkedMEADHelper(WebView view) {
        Log.i(TAG, "injectLinkedMEADHelper:  开始注入js");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String today = simpleDateFormat.format(new Date());
        view.loadUrl("javascript:" +
                "var linkedmeScript = document.createElement('script'); " +
                "linkedmeScript.src='" + LMConfig.INJECT_JS_URL + "?random=" + today + "';" +
                "linkedmeScript.onload=function(){ initLinkContent('" + LMConfig.APP_KEY
                + "','" + LMContentUtils.getDeviceId(context)
                + "','" + "1"
                + "')};" +
                "document.head.appendChild(linkedmeScript);");

        Log.i(TAG, "injectLinkedMEADHelper:  注入js完成");

    }

    /**
     * 通过 uri scheme 唤起应用
     *
     * @param url uri scheme
     */
    public void openApp(String url) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            if (intent != null) {
                PackageManager packageManager = context.getPackageManager();
                ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (info != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        } catch (URISyntaxException e) {

        }
    }

}
