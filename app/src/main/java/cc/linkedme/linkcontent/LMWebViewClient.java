package cc.linkedme.linkcontent;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cc.linkedme.linkcontent.linkcontentutils.OnLoadUrlListener;

import static android.content.Context.CLIPBOARD_SERVICE;

public class LMWebViewClient extends WebViewClient {

    private static final String TAG = "link_content";
    private Context context;
    // 是否在已有的 webview 中加载链接，true: 在已有的 webview 中加载 false: 重新创建新的 webview 加载链接
    private boolean loadUrlInSelf;
    // 注入 js sdk 层数计数
    private int initInjectPageIndex = 0;
    private int injectPageCount = initInjectPageIndex;
    private int NEED_INJECT_PAGE_COUNT = 2;
    private String firstUrl = "";

    public LMWebViewClient(Context context, boolean loadUrlInSelf) {
        this.context = context;
        this.loadUrlInSelf = loadUrlInSelf;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (loadUrlInSelf) {
            Log.i(TAG, " shouldOverrideUrlLoading()=" + url);
        }
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
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i(TAG, "onPageStarted:  开始加载页面");
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.i(TAG, "onPageFinished: 页面加载完成");
        super.onPageFinished(view, url);
        if (firstUrl.equals(url)) {
            injectPageCount = initInjectPageIndex;
        }

        if (loadUrlInSelf && url.equals(view.getOriginalUrl()) && injectPageCount < NEED_INJECT_PAGE_COUNT) {
            Log.i(TAG, "onPageFinished: url =" + url + ", view.getOriginalUrl()=" + view.getOriginalUrl());
            if (TextUtils.isEmpty(firstUrl)) {
                firstUrl = url;
            }
            injectLinkedMEADHelper(view);
            injectPageCount++;

        }

    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
        Log.i(TAG, "onPageCommitVisible: onPageCommitVisible");
    }

    @Override
    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        Log.i(TAG, "onRenderProcessGone: onRenderProcessGone");

        return super.onRenderProcessGone(view, detail);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
//        Log.i(TAG, "onLoadResource: onLoadResource==" + url);
        super.onLoadResource(view, url);
    }

    // 注入js函数监听
    public void injectLinkedMEADHelper(WebView view) {
        Log.i(TAG, "injectLinkedMEADHelper:  开始注入js");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String today = simpleDateFormat.format(new Date());
        String appKey = "7e289a2484f4368dbafbd1e5c7d06903";
        view.loadUrl("javascript:" +
                "var linkedmeScript = document.createElement('script'); " +
                "linkedmeScript.src='https://content.linkedme.cc/feed/content_sdk.js?random=" + today + "';" +
                "linkedmeScript.onload=function(){ initLinkContent('" + appKey
                + "','" + Utils.getDeviceId(context)
                + "','" + "1"
                + "')};" +
                "document.head.appendChild(linkedmeScript);");

        Log.i(TAG, "injectLinkedMEADHelper:  注入js完成");

    }

    public void showAlert(final String info) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Uri Scheme 跳转提示").setMessage("以下为捕获到的Uri Scheme\n\n" + info + "\n\n 是否跳转？").setPositiveButton("跳转", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openApp(info);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setNeutralButton("复制", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager cbm = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                        cbm.setPrimaryClip(ClipData.newPlainText("uri scheme", info));
                        Toast.makeText(context, "已复制到剪切板", Toast.LENGTH_SHORT).show();
                    }
                }).create();
        alertDialog.show();
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
