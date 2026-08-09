package com.taobao.arthas.core.util;

/**
 * Copied from {@link org.apache.commons.io.IOUtils}
 * @author ralf0131 2016-12-28 11:41.
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * 流读写辅助工具（源自 Apache Commons IO，供 Agent 内嵌使用）。
 */
public class IOUtils {

    private static final int EOF = -1;

    /**
     * {@link #copyLarge(InputStream, OutputStream)} 使用的默认缓冲区大小（{@value} 字节）。
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * 将 {@link InputStream} 全部读入并返回 {@code byte[]}。
     * <p>
     * 内部自带缓冲，无需额外包装 {@code BufferedInputStream}。
     *
     * @param input  输入流
     * @return 字节数组
     * @throws NullPointerException input 为 null
     * @throws IOException I/O 错误
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }


    /**
     * 将输入流复制到输出流（缓冲复制）。
     * <p>
     * 超过 2GB 时返回值可能为 {@code -1}，大流请使用 {@link #copyLarge}。
     *
     * @param input  输入流
     * @param output  输出流
     * @return 复制的字节数，或超过 int 上限时为 -1
     * @throws NullPointerException 入参或出参为 null
     * @throws IOException I/O 错误
     * @since 1.1
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * 大文件流复制（支持超过 2GB），使用 {@link #DEFAULT_BUFFER_SIZE} 缓冲。
     *
     * @param input  输入流
     * @param output  输出流
     * @return 复制的总字节数
     * @throws NullPointerException 入参或出参为 null
     * @throws IOException I/O 错误
     * @since 1.3
     */
    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * 使用调用方提供的 buffer 将输入流复制到输出流。
     *
     * @param input  输入流
     * @param output  输出流
     * @param buffer 复制用缓冲区
     * @return 复制的总字节数
     * @throws NullPointerException 入参或出参为 null
     * @throws IOException I/O 错误
     * @since 2.2
     */
    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * 按平台默认编码将 {@link InputStream} 读成字符串（按行拼接，行间保留 {@code \n}）。
     *
     * @param input  输入流
     * @return 文本内容
     * @throws NullPointerException input 为 null
     * @throws IOException I/O 错误
     */
    public static String toString(InputStream input) throws IOException {
        BufferedReader br = null;
        try {
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }


}
