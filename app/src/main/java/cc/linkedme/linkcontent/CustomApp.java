package cc.linkedme.linkcontent;

import android.app.Application;
import android.content.Context;

import com.bun.miitmdid.core.JLibrary;

public class CustomApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        JLibrary.InitEntry(base);
    }
}
