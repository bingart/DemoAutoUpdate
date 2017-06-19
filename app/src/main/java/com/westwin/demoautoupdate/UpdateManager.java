package com.westwin.demoautoupdate;

        import android.os.Environment;
        import android.os.Handler;
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

    private int mVersionCode;
    private int mServerVersionCode;

    private String mApkName;
    private String mServerApkName;

    private Object mLocker;

    public int getVersionCode() { return mVersionCode; }
    public int getServerVersionCode() { return mServerVersionCode; }

    public UpdateManager(String versionName) {
        try {
            mVersionCode = Integer.parseInt(versionName.replace(".", ""));
            mServerVersionCode = -1;
            mApkName = Common.APK_FILE_NAME;
            mServerApkName = Common.APK_URL_FILE_NAME;
            mLocker = new Object();
        } catch (Exception e) {
            Log.e("nutch", e.getMessage());
        }
    }

    public void start() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean ok = false;
                try {
                    int newServerVersionCode = getVersionCode();
                    if (newServerVersionCode > mVersionCode) {
                        // Notify super to wait more time
                        mServerVersionCode = 0;
                        Log.e("nutch", "update, new version code found, " + newServerVersionCode);
                        download();
                        Log.e("nutch", "update, apk downloaded");
                        mServerVersionCode = newServerVersionCode;
                        ok = true;
                    }
                } catch (Exception ex) {
                    Log.e("nutch", "update error, ex=" + ex.getMessage());
                } finally {
                    if (!ok) {
                        mServerVersionCode = mVersionCode;
                    }
                }
            }

            private int getVersionCode() throws Exception {
                try {
                    int serverVersionCode = 0;
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://dispatcher.tlvstream.com/version.txt")
                            .build();
                    Log.e("nutch", "execute");

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        String body = response.body().string();
                        serverVersionCode = Integer.parseInt(body);
                        Log.e("nutch", "serverVersionCode is " + serverVersionCode);
                        return serverVersionCode;
                    }
                } catch (IOException e) {
                    throw new Exception();
                }
            }

            private void download() throws Exception {
                try {
                    int serverVersionCode = 0;
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://dispatcher.tlvstream.com/Storage/" + mServerApkName)
                            .build();
                    Log.d("nutch", "download, wait ...");

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        // 读取文件
                        InputStream is = null;
                        byte[] buffer = new byte[1024];
                        int len;
                        long currentTotalLen = 0L;
                        FileOutputStream fos = null;
                        try {
                            is = response.body().byteStream();
                            File file = new File(Environment.getExternalStorageDirectory(), mApkName);
                            if (file.exists()) {
                                // 如果文件存在 则删除
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
                            Log.d("nutch", "download, write file size " + currentTotalLen);
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
