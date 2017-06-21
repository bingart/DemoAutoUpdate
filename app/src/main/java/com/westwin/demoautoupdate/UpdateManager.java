package com.westwin.demoautoupdate;

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by feisun on 2017/1/2.
 */
public class UpdateManager {

    private String mServerAppUrl;
    private String mVersionFileName;
    private String mLocalApkFileName;
    private String mServerApkFileName;

    public UpdateManager(
            String serverAppUrl,
            String versionFileName,
            String localApkFileName,
            String serverApkFileName) {
        try {
            mServerAppUrl = serverAppUrl;
            mVersionFileName = versionFileName;
            mLocalApkFileName = localApkFileName;
            mServerApkFileName = serverApkFileName;
        } catch (Exception e) {
            Log.e("nutch", e.getMessage());
        }
    }

    public boolean isNewVersionFound(PackageInfo packageInfo) {
        try {
            String currentVersionStr = FileHelper.readFileConent(
                    Environment.getExternalStorageDirectory(),
                    Common.APK_VERSION_FILE_NAME);
            int currentVesion = Integer.parseInt(currentVersionStr);
            String serverVersionStr = FileHelper.readFileConent(
                    Environment.getExternalStorageDirectory(),
                    mVersionFileName);
            int serverVesion = Integer.parseInt(serverVersionStr);
            if (serverVesion > currentVesion) {
                return true;
            }
        } catch (Exception ex) {
            Log.e("nutch", "isNewVersionFound error, ex=" + ex);
        }
        return false;
    }

    public void start() {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    FileHelper.download(
                            mServerAppUrl, mVersionFileName,
                            Environment.getExternalStorageDirectory(), mVersionFileName);
                    FileHelper.download(
                            mServerAppUrl, mServerApkFileName,
                            Environment.getExternalStorageDirectory(), mLocalApkFileName);
                } catch (Exception ex) {
                    Log.e("nutch", "update error, ex=" + ex.getMessage());
                } finally {
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }
}
