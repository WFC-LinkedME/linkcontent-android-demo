package cc.linkedme.linkcontent.linkcontentutils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.UUID;

public class LMContentUtils {

    //没有网络连接
    private static final int NETWORN_NONE = -1;

    //手机网络数据连接类型
    //未知蜂窝网络
    private static final int NETWORN_MOBILE = 0;
    //wifi连接
    private static final int NETWORN_WIFI = 1;
    private static final int NETWORN_2G = 2;
    private static final int NETWORN_3G = 3;
    private static final int NETWORN_4G = 4;

    /**
     * 获取当前网络连接类型
     */
    public static int getNetworkState(Context mContext) {
        try {
            //获取系统的网络服务
            ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            //如果当前没有网络
            if (null == connManager)
                return NETWORN_NONE;

            //获取当前网络类型，如果为空，返回无网络
            NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
            if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
                return NETWORN_NONE;
            }
            // 判断是不是连接的是不是wifi
            if (getWifiConnected(mContext)) {
                return NETWORN_WIFI;
            }

            // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
            NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (null != networkInfo) {
                NetworkInfo.State state = networkInfo.getState();
                String strSubTypeName = networkInfo.getSubtypeName();
                if (null != state)
                    if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                        switch (activeNetInfo.getSubtype()) {
                            //如果是2g类型
                            case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                            case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                            case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                                return NETWORN_2G;
                            //如果是3g类型
                            case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                            case TelephonyManager.NETWORK_TYPE_HSPA:
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            case TelephonyManager.NETWORK_TYPE_EHRPD:
                            case TelephonyManager.NETWORK_TYPE_HSPAP:
                                return NETWORN_3G;
                            //如果是4g类型
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                return NETWORN_4G;
                            default:
                                //中国移动 联通 电信 三种3G制式
                                if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                    return NETWORN_3G;
                                } else {
                                    return NETWORN_MOBILE;
                                }
                        }
                    }
            }
        } catch (Exception ignore) {

        }
        return NETWORN_NONE;
    }

    private static boolean getWifiConnected(Context mContext) {
        try {
            if (PackageManager.PERMISSION_GRANTED == mContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
                ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Network[] networkInfos = connManager.getAllNetworks();
                    for (Network network : networkInfos) {
                        NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            return true;
                        }
                    }
                } else {
                    NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    return ((wifiInfo != null) && wifiInfo.isConnected());
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    public static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                deviceId = telephonyManager.getDeviceId();
            }
        } catch (Exception ignore) {

        }
        return deviceId;
    }

    public static String getAppVersion(Context context) {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignore) {

        }
        return appVersionName;
    }

    public static String getAuid(Context context) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("auid_info", Context.MODE_PRIVATE);
        String auid = sharedPreferences.getString("auid", "");
        if (TextUtils.isEmpty(auid)) {
            auid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString("auid", auid).apply();
        }
        return auid;
    }

}
