package com.taobao.arthas.core.shell.command.internal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.taobao.arthas.core.util.LogUtil;

/**
 * Shell 输出重定向处理器：将命令结果写入文件或日志。
 * <p>
 * 继承 {@link PlainTextHandler} 先剥离 ANSI；实现 {@link CloseFunction} 在 Job 结束时关闭流。
 * 未指定文件时回退到 {@link LogUtil#getResultLogger()}。
 *
 * @author gehui 2017年7月27日 上午11:38:40
 * @author hengyunabc 2019-02-06
 */
public class RedirectHandler extends PlainTextHandler implements CloseFunction {
    private PrintWriter out;

    private File file;

    public RedirectHandler() {

    }

    /**
     * 打开目标文件；目录不存在时递归创建父目录。
     * @param name 目标文件路径
     * @param append 为 true 时追加写入
     */
    public RedirectHandler(String name, boolean append) throws IOException {
        File file = new File(name);

        // 目标是目录则拒绝，与 shell 重定向语义一致
        if (file.isDirectory()) {
            throw new IOException(name + ": Is a directory");
        }

        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
        }
        this.file = file;
        out = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
    }

    @Override
    /** 写文件或日志，并原样返回 data 供下游管道继续处理 */
    public String apply(String data) {
        data = super.apply(data);
        if (out != null) {
            out.write(data);
            out.flush();
        } else {
            LogUtil.getResultLogger().info(data);
        }
        return data;
    }

    @Override
    /** 关闭 {@link PrintWriter}，释放文件句柄 */
    public void close() {
        if (out != null) {
            out.close();
        }
    }

    /** @return 重定向目标的绝对路径 */
    public String getFilePath() {
        return file.getAbsolutePath();
    }
}
