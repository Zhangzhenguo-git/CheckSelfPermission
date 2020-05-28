package com.zhang.checkselfpermission;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CheckPermissionUtil mCheckPermissionUtil;
    private String [] permissions;

    //权限2 创建监听权限的接口对象
    CheckPermissionUtil.IPermissionsResult permissionsResult=new CheckPermissionUtil.IPermissionsResult() {
        @Override
        public void passPermissons() {
            Toast.makeText(MainActivity.this, "权限通过，可以做其他事情!", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void forbitPermissons() {
            System.out.println("执行finish");
        }

        @Override
        public void restPermissons() {
            mCheckPermissionUtil.chekPermissions(MainActivity.this, permissions, permissionsResult);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissions=new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        mCheckPermissionUtil=CheckPermissionUtil.getInstance();
        mCheckPermissionUtil.chekPermissions(this, permissions, permissionsResult);
    }
    //权限3 在Activity重写onRequestPermissionsResult方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //就多一个参数this
        mCheckPermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
