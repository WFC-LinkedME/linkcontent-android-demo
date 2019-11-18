package cc.linkedme.linkcontent.linkcontentutils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class LMWebChromeClient extends WebChromeClient {

    private Context context;

    public LMWebChromeClient(Context context) {
        this.context = context;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (context instanceof OnReceivedTitleListener) {
            ((OnReceivedTitleListener) context).onReceivedTitle(title);
        }
    }

    // 处理h5中alert提示
    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Alert");
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                result.confirm();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
        return true;
    }
}
