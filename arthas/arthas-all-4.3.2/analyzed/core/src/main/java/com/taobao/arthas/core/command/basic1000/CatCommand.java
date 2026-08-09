package com.taobao.arthas.core.command.basic1000;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.taobao.arthas.core.command.model.CatModel;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.cli.CompletionUtils;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.FileUtils;
import com.taobao.middleware.cli.annotations.Argument;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Option;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * 文件内容查看命令：类似 Unix cat，按指定编码读取一个或多个本地文件并输出。
 */
@Name("cat")
@Summary("Concatenate and print files")
public class CatCommand extends AnnotatedCommand {
    private static final Logger logger = LoggerFactory.getLogger(CatCommand.class);
    /** 待读取的文件路径列表 */
    private List<String> files;
    /** 文件字符编码，null 时使用 JVM 默认编码 */
    private String encoding;
    /** 单文件读取大小上限（字节），默认 128KB */
    private Integer sizeLimit = 128 * 1024;
    /** sizeLimit 可配置的最大值（8MB） */
    private int maxSizeLimit = 8 * 1024 * 1024;

    @Argument(argName = "files", index = 0)
    @Description("files")
    public void setFiles(List<String> files) {
        this.files = files;
    }

    @Option(longName = "encoding")
    @Description("File encoding")
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Option(shortName = "M", longName = "sizeLimit")
    @Description("Upper size limit in bytes for the result (128 * 1024 by default, the maximum value is 8 * 1024 * 1024)")
    public void setSizeLimit(Integer sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    @Override
    public void process(CommandProcess process) {
        if (!verifyOptions(process)) {
            return;
        }

        // 先校验所有路径是否存在且为普通文件
        for (String file : files) {
            File f = new File(file);
            if (!f.exists()) {
                process.end(-1, "cat " + file + ": No such file or directory");
                return;
            }
            if (f.isDirectory()) {
                process.end(-1, "cat " + file + ": Is a directory");
                return;
            }
        }

        for (String file : files) {
            File f = new File(file);
            if (f.length() > sizeLimit) {
                process.end(-1, "cat " + file + ": Is too large, size: " + f.length());
                return;
            }
            try {
                String fileToString = FileUtils.readFileToString(f,
                        encoding == null ? Charset.defaultCharset() : Charset.forName(encoding));
                process.appendResult(new CatModel(file, fileToString));
            } catch (IOException e) {
                logger.error("cat read file error. name: " + file, e);
                process.end(1, "cat read file error: " + e.getMessage());
                return;
            }
        }

        process.end();
    }

    /** 校验 sizeLimit 是否在允许范围内（非 TTY 会话有更严格上限） */
    private boolean verifyOptions(CommandProcess process) {
        if (sizeLimit > maxSizeLimit) {
            process.end(-1, "sizeLimit cannot be large than: " + maxSizeLimit);
            return false;
        }

        //目前不支持过滤，限制http请求执行的文件大小
        int maxSizeLimitOfNonTty = 128 * 1024;
        if (!process.session().isTty() && sizeLimit > maxSizeLimitOfNonTty) {
            process.end(-1, "When executing in non-tty session, sizeLimit cannot be large than: " + maxSizeLimitOfNonTty);
            return false;
        }
        return true;
    }

    @Override
    public void complete(Completion completion) {
        if (!CompletionUtils.completeFilePath(completion)) {
            super.complete(completion);
        }
    }

}
