package com.taobao.arthas.core.command.basic1000;


import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.*;

/**
 * 管道 {@code tee} 命令：将上游命令输出同时写入文件并向下游传递。
 * <p>
 * 本类仅声明参数供管道解析器使用，{@link #process} 直接报错提示仅用于管道场景。
 *
 * @author min.yang
 */
@Name("tee")
@Summary("tee command for pipes." )
@Description(Constants.EXAMPLE +
        " sysprop | tee /path/to/logfile | grep java \n" +
        " sysprop | tee -a /path/to/logfile | grep java \n"
        + Constants.WIKI + Constants.WIKI_HOME + "tee")
public class TeeCommand extends AnnotatedCommand {

    /** 输出目标文件路径 */
    private String filePath;
    /** 是否以追加模式写入文件 */
    private boolean append;

    @Argument(index = 0, argName = "file", required = false)
    @Description("File path")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Option(shortName = "a", longName = "append", flag = true)
    @Description("Append to file")
    public void setRegEx(boolean append) {
        this.append = append;
    }

    /** 非管道直接调用时提示用法错误 */
    @Override
    public void process(CommandProcess process) {
        process.end(-1, "The tee command only for pipes. See 'tee --help'");
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isAppend() {
        return append;
    }
}
