package org.keycloak.testframework.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 测试框架使用的轻量级文件读写与删除工具。
 * <p>
 * 封装 Apache Commons IO，将 IO 异常包装为 {@link RuntimeException}。
 */
public class FileUtils {

    /**
     * 读取 UTF-8 文本文件并解析为 {@code long}。
     *
     * @param file 源文件
     * @return 解析后的长整型值
     */
    public static long readLongFromFile(File file) {
        return Long.parseLong(readStringFromFile(file));
    }

    /**
     * 以 UTF-8 读取整个文件为字符串。
     *
     * @param file 源文件
     * @return 文件内容
     */
    public static String readStringFromFile(File file) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 将长整型值以十进制字符串写入文件。 */
    public static void writeToFile(File file, long value) {
        writeToFile(file, Long.toString(value));
    }

    /**
     * 将字符串写入文件（覆盖已有内容）。
     *
     * @param file 目标文件
     * @param value 要写入的文本
     */
    public static void writeToFile(File file, String value) {
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(file, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文件或递归删除目录。
     *
     * @param file 要删除的路径
     * @throws RuntimeException 删除失败时
     */
    public static void delete(File file) {
        if (file.isFile()) {
            if (!file.delete()) {
                throw new RuntimeException("Failed to delete file: " + file.getAbsolutePath());
            }
        } else if (file.isDirectory()) {
            try {
                org.apache.commons.io.FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete directory: " + e);
            }
        }

    }

}
