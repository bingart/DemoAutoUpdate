package com.westwin.demoautoupdate;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private UpdateManager mUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);
        FileHelper.writeFileContent(Environment.getExternalStorageDirectory(), "aaa.txt", "bbbb");
        String content = FileHelper.getFileConent(Environment.getExternalStorageDirectory(), "aaa.txt");
        TextView helloTextView = (TextView) findViewById(R.id.helloTextView);
        if (helloTextView != null) {
            helloTextView.setText(content);
        }

        try {
            mUpdateManager =
                    new UpdateManager(
                            "http://www.mopinfo.com/app",
                            Common.APK_VERSION_FILE_NAME,
                            Common.APK_SERVER_FILE_NAME,
                            Common.APK_LOCAL_FILE_NAME);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (mUpdateManager.isNewVersionFound(pInfo)) {
                //dialog();
            } else {
                //mUpdateManager.start();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("发现新版本，是否更新 ?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * 安装应用
     */
    public void updateApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
                Uri.fromFile(new File(Environment.getExternalStorageDirectory(), Common.APK_LOCAL_FILE_NAME)),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void updateApkSilently() {
        try {
            String apkFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Common.APK_LOCAL_FILE_NAME;
            Runtime.getRuntime().exec(new String[]{"su", "-c", "pm install -r " + apkFilePath});
        } catch (IOException e) {
            Log.e("nutch", "no root");
        }
    }
}