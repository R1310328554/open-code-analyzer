package com.taobao.arthas.core.util;

/**
 * Copied from {@link org.apache.commons.io.FileUtils}
 * @author ralf0131 2016-12-28 11:46.
 */
import io.termd.core.util.Helper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.taobao.arthas.common.ArthasConstants;

/**
 * 文件读写与 Shell 命令历史持久化工具（部分逻辑源自 Commons IO）。
 */
public class FileUtils {

    /**
     * 将字节数组写入文件；文件不存在则创建，并自动创建父目录。
     *
     * @param file  目标文件
     * @param data  待写入内容
     * @throws IOException I/O 异常
     * @since 1.1
     */
    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

    /**
     * 将字节数组写入文件，可选择追加模式。
     *
     * @param file  目标文件
     * @param data  待写入内容
     * @param append 为 {@code true} 时在文件末尾追加，否则覆盖
     * @throws IOException I/O 异常
     * @since IO 2.1
     */
    public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            out.write(data);
        }
        // ignore
    }

    /**
     * 打开文件的 {@link FileOutputStream}，必要时创建父目录与文件本身。
     * <p>
     * 若路径已存在且为目录、或不可写、或父目录创建失败，则抛出 {@link IOException}。
     *
     * @param file  输出目标，不能为 {@code null}
     * @param append 是否追加写入
     * @return 新打开的 FileOutputStream
     * @throws IOException 路径为目录、不可写或 mkdirs 失败
     * @since 2.1
     */
    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }

    /** 判断是否为 auth 命令（需脱敏密码，仅保留命令名） */
    private static boolean isAuthCommand(String command) {
        // 需要改写 auth command, TODO 更准确应该是用mask去掉密码信息
        return command != null && command.trim().startsWith(ArthasConstants.AUTH);
    }

    /**
     * 将终端命令历史（code point 数组形式）覆盖写入文件。
     * <p>
     * auth 命令行会被替换为 {@link ArthasConstants#AUTH}，避免明文密码落盘。
     *
     * @param history 历史记录，每条为 int[] code points
     * @param file 目标文件
     */
    public static void saveCommandHistory(List<int[]> history, File file) {
        try (OutputStream out = new BufferedOutputStream(openOutputStream(file, false))) {
            for (int[] command : history) {
                String commandStr = Helper.fromCodePoints(command);
                if (isAuthCommand(commandStr)) {
                    commandStr = ArthasConstants.AUTH;
                }

                out.write(commandStr.getBytes(StandardCharsets.UTF_8));
                out.write('\n');
            }
        } catch (IOException e) {
            // ignore
        }
        // ignore
    }

    /** 从文件加载命令历史（code point 列表）；失败时返回已读部分或空列表 */
    public static List<int[]> loadCommandHistory(File file) {
        BufferedReader br = null;
        List<int[]> history = new ArrayList<>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                history.add(Helper.toCodePoints(line));
            }
        } catch (IOException e) {
            // ignore
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
        return history;
    }

    /**
     * 将字符串形式的命令历史覆盖写入文件（跳过空行，auth 命令脱敏）。
     *
     * @param history 命令字符串列表
     * @param file 目标文件
     */
    public static void saveCommandHistoryString(List<String> history, File file) {
        try (OutputStream out = new BufferedOutputStream(openOutputStream(file, false))) {
            for (String command : history) {
                if (!StringUtils.isBlank(command)) {
                    if (isAuthCommand(command)) {
                        command = ArthasConstants.AUTH;
                    }
                    out.write(command.getBytes("utf-8"));
                    out.write('\n');
                }
            }
        } catch (IOException e) {
            // ignore
        }
        // ignore
    }

    /** 从文件加载字符串命令历史（忽略空行） */
    public static List<String> loadCommandHistoryString(File file) {
        BufferedReader br = null;
        List<String> history = new ArrayList<>();
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (!StringUtils.isBlank(line)) {
                    history.add(line);
                }
            }
        } catch (IOException e) {
            // ignore
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
        return history;
    }

    /**
     * 按指定字符集读取整个文本文件为字符串。
     *
     * @param file 源文件
     * @param encoding 字符集
     * @return 文件全文
     * @throws IOException 读取失败
     */
    public static String readFileToString(File file, Charset encoding) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            Reader reader = new BufferedReader(new InputStreamReader(stream, encoding));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        }
    }

    /** 从路径加载 {@link Properties} 配置文件 */
    public static Properties readProperties(String file) throws IOException {
        Properties properties = new Properties();

        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            properties.load(in);
            return properties;
        } finally {
            com.taobao.arthas.common.IOUtils.close(in);
        }

    }

    /**
     * 判断路径不存在或为目录（用于校验输出路径不会误覆盖普通文件）。
     *
     * @param path 文件系统路径
     * @return {@code true} 表示路径不存在或已是目录
     */
    public static boolean isDirectoryOrNotExist(String path) {
        File file = new File(path);
        return !file.exists() || file.isDirectory();
    }
}
