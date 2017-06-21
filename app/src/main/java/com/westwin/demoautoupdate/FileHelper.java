package com.westwin.demoautoupdate;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public static String getFileConent(File path, String fileName) {
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
}
