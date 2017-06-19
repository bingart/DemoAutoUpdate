package com.westwin.demoautoupdate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private TextView mVersionText;

    private String mUpdateMessage;
    private TextView mUpdateText;
    private UpdateManager mUpdateManager;

    private int mRunCount = 0;
    private int mExpiredRunCount = 30;
    private boolean mCheckUpdateFlag = true;
    private Handler nHandler = new Handler();

    private Runnable mTask = new Runnable() {
        public void run() {

            mRunCount++;

            if (mCheckUpdateFlag && mRunCount <= mExpiredRunCount) {
                nHandler.postDelayed(this, 1000);
                int newServerVersionCode = mUpdateManager.getServerVersionCode();
                if (newServerVersionCode == -1) {
                    Log.d("nutch", "continue and wait for new version code ...");
                } else if (newServerVersionCode == 0) {
                    Log.d("nutch", "continue and wait for download ...");
                    mUpdateMessage += ", version get";
                    mUpdateText.setText(mUpdateMessage);
                    // larger the expired time for download
                    mExpiredRunCount = 60;
                } else {
                    mCheckUpdateFlag = false;
                    mUpdateMessage += ", server version " + newServerVersionCode;
                    mUpdateText.setText(mUpdateMessage);
                    if (newServerVersionCode > mUpdateManager.getVersionCode()) {
                        // updateApk();
                        updateApkSilently();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);
        writeTest();

        String version = null;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception ex) {
        }
        mUpdateManager = new UpdateManager(version);

        mVersionText = (TextView) findViewById(R.id.versionTextView);
        mVersionText.setText(Integer.toString(mUpdateManager.getVersionCode()));

        mUpdateMessage = "Init";
        mUpdateText = (TextView) findViewById(R.id.updateTextView);
        mUpdateText.setText(mUpdateMessage);

        nHandler.postDelayed(mTask, 1000);
        mUpdateManager.start();
    }

    /**
     * Checks if the app has permission to write to device storage
     *
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
    public void updateApk(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
                Uri.fromFile(new File(Environment.getExternalStorageDirectory(), Common.APK_FILE_NAME)),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void updateApkSilently() {
        try {
            String apkFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Common.APK_FILE_NAME;
            Runtime.getRuntime().exec(new String[] {"su", "-c", "pm install -r " + apkFilePath});
            mUpdateMessage += ", install update";
            mUpdateText.setText(mUpdateMessage);
        } catch (IOException e) {
            Log.e("nutch", "no root");
        }
    }

    private void writeTest() {
        FileOutputStream fos = null;
        try {
            String state = Environment.getExternalStorageState();
            File file = new File(Environment.getExternalStorageDirectory(), "aaaa.txt");
            if (file.exists()) {
                // 如果文件存在 则删除
                file.delete();
            } else {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            byte[] bytes = "aaaaaaaaaaaaaaa".getBytes(Charset.forName("UTF8"));
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
