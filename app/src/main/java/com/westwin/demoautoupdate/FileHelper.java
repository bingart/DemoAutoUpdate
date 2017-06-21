package com.westwin.demoautoupdate;

import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by feisun on 2017/6/21.
 */
public class FileHelper {

    /**
     * Write string to file.
     * @param path
     * @param fileName
     * @param content
     */
    public static void writeFileContent(File path, String fileName, String content) {
        FileOutputStream fos = null;
        try {
            String state = Environment.getExternalStorageState();
            File file = new File(path, fileName);
            if (file.exists()) {
                // 如果文件存在 则删除
                file.delete();
            } else {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            byte[] bytes = content.getBytes(Charset.forName("UTF8"));
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

    /**
     * Read string from file.
     * @param path
     * @param fileName
     * @return
     */
    public static String readFileConent(File path, String fileName) {
        FileInputStream fin = null;
        try {
            String state = Environment.getExternalStorageState();
            File file = new File(path, fileName);
            if (file.exists()) {
                fin = new FileInputStream(file);
                int length = fin.available();
                byte [] buffer = new byte[length];
                fin.read(buffer);
                String str = new String(buffer, 0, length, "UTF-8");
                return str;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void download(String serverAppUrl, String serverFileName, File path, String localFileName) throws Exception {
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
                    File file = new File(path, localFileName);
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
}
