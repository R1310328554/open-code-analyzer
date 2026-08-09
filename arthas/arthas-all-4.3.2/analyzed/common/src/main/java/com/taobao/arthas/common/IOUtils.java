package com.taobao.arthas.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * IO 辅助工具：流复制、关闭、ZIP 解压及路径父子关系校验。
 *
 * @author hengyunabc 2018-11-06
 */
public class IOUtils {

    private IOUtils() {
    }

    /** 将输入流按 UTF-8 读成字符串 */
    public static String toString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    /** 1024 字节缓冲循环复制 in → out */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    /**
     * 读取输入流全部内容为字节数组。
     * @return 字节数组
     * @throws java.io.IOException 读失败
     */
    public static byte[] getBytes(InputStream input) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        copy(input, result);
        result.close();
        return result.toByteArray();
    }

    public static IOException close(InputStream input) {
        return close((Closeable) input);
    }

    public static IOException close(OutputStream output) {
        return close((Closeable) output);
    }

    public static IOException close(final Reader input) {
        return close((Closeable) input);
    }

    public static IOException close(final Writer output) {
        return close((Closeable) output);
    }

    /** 安全关闭 Closeable，异常作为返回值而非抛出（便于 finally 链式处理） */
    public static IOException close(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            return ioe;
        }
        return null;
    }

    // JDK6 无 ZipFile implements AutoCloseable，单独提供 close
    public static IOException close(final ZipFile zip) {
        try {
            if (zip != null) {
                zip.close();
            }
        } catch (final IOException ioe) {
            return ioe;
        }
        return null;
    }

    /** 规范路径下判断 child 是否在 parent 目录树内（防 Zip Slip） */
    public static boolean isSubFile(File parent, File child) throws IOException {
        return child.getCanonicalPath().startsWith(parent.getCanonicalPath() + File.separator);
    }
 
    public static boolean isSubFile(String parent, String child) throws IOException {
        return isSubFile(new File(parent), new File(child));
    }

    /** 解压 ZIP 到目标目录，校验条目路径不逃逸出 extractFolder */
    public static void unzip(String zipFile, String extractFolder) throws IOException {
        File file = new File(zipFile);
        ZipFile zip = null;
        try {
            int BUFFER = 1024 * 8;

            zip = new ZipFile(file);
            File newPath = new File(extractFolder);
            newPath.mkdirs();

            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                File destFile = new File(newPath, currentEntry);
                if (!isSubFile(newPath, destFile)) {
                    throw new IOException("Bad zip entry: " + currentEntry);
                }

                // destFile = new File(newPath, destFile.getName());
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

                if (!entry.isDirectory()) {
                    BufferedInputStream is = null;
                    BufferedOutputStream dest = null;
                    try {
                        is = new BufferedInputStream(zip.getInputStream(entry));
                        int currentByte;
                        // establish buffer for writing file
                        byte data[] = new byte[BUFFER];

                        // write the current file to disk
                        FileOutputStream fos = new FileOutputStream(destFile);
                        dest = new BufferedOutputStream(fos, BUFFER);

                        // read and write until last byte is encountered
                        while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                    } finally {
                        close(dest);
                        close(is);
                    }

                }

            }
        } finally {
            close(zip);
        }

    }
}
