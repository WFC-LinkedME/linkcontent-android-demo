package cc.linkedme.linkcontent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.IIdentifierListener;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.supplier.IdSupplier;

public class MainActivity extends AppCompatActivity {

    private Button feed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int nres = MdidSdkHelper.InitSdk(this, true, new IIdentifierListener() {
            @Override
            public void OnSupport(boolean isSupport, IdSupplier idSupplier) {
                if (idSupplier == null) {
                    return;
                }
                String oaid = idSupplier.getOAID();
                Log.i("LinkContent", "OnSupport: oaid=" + oaid);
                SharedPreferences sharedPreferences = getSharedPreferences("device_info", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("oaid", oaid);
                editor.apply();
                idSupplier.shutDown();
            }
        });
        if (nres == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT) {//不支持的设备

        } else if (nres == ErrorCode.INIT_ERROR_LOAD_CONFIGFILE) {//加载配置文件出错

        } else if (nres == ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT) {//不支持的设备厂商

        } else if (nres == ErrorCode.INIT_ERROR_RESULT_DELAY) {//获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程

        } else if (nres == ErrorCode.INIT_HELPER_CALL_ERROR) {//反射调用出错

        }
        Log.d(getClass().getSimpleName(), "return value: " + String.valueOf(nres));
        feed = findViewById(R.id.feed);
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                startActivity(intent);
            }
        });
    }
}
