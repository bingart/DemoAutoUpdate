package com.westwin.demoautoupdate;

        import android.content.pm.PackageInfo;
        import android.os.Environment;
        import android.os.Handler;
        import android.util.Log;

        import com.squareup.okhttp.OkHttpClient;
        import com.squareup.okhttp.Request;
        import com.squareup.okhttp.Response;

        import java.io.File;
        import java.io.FileInputStream;
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
            String currentVersionStr = FileHelper.getFileConent(
                    Environment.getExternalStorageDirectory(),
                    Common.APK_VERSION_FILE_NAME);
            int currentVesion = Integer.parseInt(currentVersionStr);
            String serverVersionStr = FileHelper.getFileConent(Environment.getExternalStorageDirectory(), mVersionFileName);
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
                    download(mServerAppUrl, mVersionFileName, mVersionFileName);
                    download(mServerAppUrl, mServerApkFileName, mLocalApkFileName);
                } catch (Exception ex) {
                    Log.e("nutch", "update error, ex=" + ex.getMessage());
                } finally {
                }
            }

            private void download(String serverAppUrl, String serverFileName, String localFileName) throws Exception {
                try {
                    int serverVersionCode = 0;
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(serverAppUrl + "/app/" + serverFileName)
                            .build();
                    Log.d("nutch", String.format("download, file=%s, wait ...", serverFileName));

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        // Read file
                        InputStream is = null;
                        byte[] buffer = new byte[1024];
                        int len;
                        long currentTotalLen = 0L;
                        FileOutputStream fos = null;
                        try {
                            is = response.body().byteStream();
                            File file = new File(Environment.getExternalStorageDirectory(), localFileName);
                            if (file.exists()) {
                                // If file exists, delete it
                                file.delete();
                            } else {
                                file.createNewFile();
                            }
                            fos = new FileOutputStream(file);
                            while ((len = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                                currentTotalLen += len;
                            }
                            fos.flush();
                            Log.d("nutch", String.format("download, write file=%s, size=%d", serverFileName, currentTotalLen));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
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
                    }
                } catch (IOException e) {
                    throw new Exception();
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }
}
