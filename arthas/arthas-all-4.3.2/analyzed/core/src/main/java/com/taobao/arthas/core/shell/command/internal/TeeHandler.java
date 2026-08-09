package com.taobao.arthas.core.shell.command.internal;

import com.taobao.arthas.core.command.basic1000.TeeCommand;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.CommandLine;
import com.taobao.middleware.cli.annotations.CLIConfigurator;

import java.io.*;
import java.util.List;

/**
 * 管道 tee 处理器：将 stdout 同时写入终端与文件（类似 Unix tee）。
 * <p>
 * 通过 {@link TeeCommand} 解析路径与 append 标志；实现 {@link CloseFunction} 关闭文件流。
 *
 * @author min.yang
 */
public class TeeHandler extends StdoutHandler implements CloseFunction {
    /** 管道子命令名 */
    public static final String NAME = "tee";
    private PrintWriter out;
    private static CLI cli = null;

    /** 打开目标文件；空路径时仅透传不写文件 */
    public TeeHandler(String filePath, boolean append) throws IOException {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);

        if (file.isDirectory()) {
            throw new IOException(filePath + ": Is a directory");
        }

        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
            }
        }
        out = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
    }

    /** 从 Token 解析 {@link TeeCommand} 并构造 TeeHandler */
    public static StdoutHandler inject(List<CliToken> tokens) {
        List<String> args = StdoutHandler.parseArgs(tokens, NAME);

        TeeCommand teeCommand = new TeeCommand();
        if (cli == null) {
            cli = CLIConfigurator.define(TeeCommand.class);
        }
        CommandLine commandLine = cli.parse(args, true);

        try {
            CLIConfigurator.inject(commandLine, teeCommand);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        String filePath = teeCommand.getFilePath();
        boolean append = teeCommand.isAppend();
        try {
            return new TeeHandler(filePath, append);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    /** 写入文件并原样返回 data，保证下游管道仍能收到输出 */
    public String apply(String data) {
        data = super.apply(data);
        if (out != null) {
            out.write(data);
            out.flush();
        }
        return data;
    }

    @Override
    /** 关闭底层 {@link PrintWriter} */
    public void close() {
        if (out != null) {
            out.close();
        }
    }
}
