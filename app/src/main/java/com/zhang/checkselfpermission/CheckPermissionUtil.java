package com.zhang.checkselfpermission;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class CheckPermissionUtil {
    //饿汉式单例方式，简单，安全，效率高，缺点不能延迟加载
//    private static CheckPermissionUtiil checkPermission=new CheckPermissionUtiil;

    private static Activity mActivity;
    private SetPermission mySetPermission;
    private String[] permission = new String[]{};
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private int COUNT = 0;

    //权限请求码
    private final int mRequestCode = 100;
    public static boolean showSystemSetting = true;
    private static CheckPermissionUtil permissionsUtil;
    private IPermissionsResult mPermissionsResult;
    AlertDialog mPermissionDialog;

    //    静态内部类单例模式，线程安全，效率高，支持延迟加载
    private static class CheckPermissionInstance {
        private static final CheckPermissionUtil mInstance = new CheckPermissionUtil();
    }

    public static CheckPermissionUtil getInstance() {
        return CheckPermissionInstance.mInstance;
    }

    /**
     * 添加标记
     * @param arr
     * @param mission
     * @return
     */
    public static String[] addElementToArray(String[] arr, String mission) {
        String [] result = new String[arr.length+1];
        for(int i=0;i<arr.length;i++) {
            result[i]=arr[i];
        }
        result[result.length-1] = mission;
        return result;
    }

    public void chekPermissions(Activity context, String[] permissions, IPermissionsResult permissionsResult) {
        mPermissionsResult = permissionsResult;
        if (Build.VERSION.SDK_INT < 23) {//6.0才用动态权限
            permissionsResult.passPermissons();
            return;
        }
        //创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
        List<String> mPermissionList = new ArrayList<>();
        if (Build.VERSION.SDK_INT==29){
            permissions=addElementToArray(permissions,Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }
        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(context, permissions, mRequestCode);
        } else {
            //说明权限都已经通过，可以做你想做的事情去
            permissionsResult.passPermissons();
            return;
        }
    }

    /**
     * 请求权限后回调的方法
     *
     * @param context
     * @param requestCode  是我们自己定义的权限请求码
     * @param permissions  是我们请求的权限名称数组
     * @param grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
     */
    public void onRequestPermissionsResult(Activity context, int requestCode, String[] permissions, int[] grantResults) {
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
//                if (showSystemSetting) {
                    mPermissionsResult.restPermissons();
//                    showSystemPermissionsSettingDialog(context);//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
//                } else {
//                    mPermissionsResult.forbitPermissons();
//                }
            } else {
                //全部权限通过
                mPermissionsResult.passPermissons();
            }
        }
    }

    public interface IPermissionsResult {
        void passPermissons();
        void forbitPermissons();
        void restPermissons();
    }
    /**
     * 不再提示权限时的展示对话框
     */
    private void showSystemPermissionsSettingDialog(final Activity context) {
        final String mPackName = context.getPackageName();
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(context)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            Uri packageURI = Uri.parse("package:" + mPackName);
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            context.startActivity(intent);
                            context.finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                            //mContext.finish();
                            mPermissionsResult.forbitPermissons();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }
    //关闭对话框
    private void cancelPermissionDialog() {
        if (mPermissionDialog != null) {
            mPermissionDialog.cancel();
            mPermissionDialog = null;
        }
    }

    /**
     * 设置当前操作——View
     */
    public void setContext(String[] permissions) {
        COUNT = permissions.length;
        if (Build.VERSION.SDK_INT==29){
            permissions=addElementToArray(permissions,Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
//        全部权限值
        for (String mission : permissions) {
            System.out.println("执行" + mission);
            //        判断当前应用是否开启此权限
            if (ContextCompat.checkSelfPermission(mActivity, mission) != PackageManager.PERMISSION_GRANTED) {
//                    如果未开启，将向用户解释此权限的用处,用户看到后将再次提醒用户是否开启
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, mission)) {
                    System.out.println("执行提示用户重新启用");
                } else {
                    ActivityCompat.requestPermissions(mActivity, permissions, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                }
            } else {
                COUNT--;
                System.out.println("执行当前剩余值：" + COUNT);
            }
        }
    }


    public interface SetPermission {
        void onReqPermissionSuccess();

        void onReqPermissionError();

    }

    public void onPermission(SetPermission setPermission) {
        this.mySetPermission = mySetPermission;
    }

}
