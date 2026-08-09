package com.taobao.arthas.core.shell.command.internal;

import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.middleware.cli.CLIs;
import com.taobao.middleware.cli.CommandLine;
import com.taobao.middleware.cli.Option;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管道 wc 处理器：统计上游输出行数（当前仅支持 {@code wc -l}）。
 * <p>
 * 实现 {@link StatisticsFunction}；{@link #apply} 按行累加，Job 结束通过 {@link #result()} 输出总数。
 *
 * @author ralf0131 2017-02-23 23:28.
 */
public class WordCountHandler extends StdoutHandler implements StatisticsFunction  {

    /** 管道子命令名 */
    public static final String NAME = "wc";

    private boolean lineMode;

    private String result = null;
    private final AtomicInteger total = new AtomicInteger(0);

    /** 解析 -l 标志并构造 WordCountHandler */
    public static StdoutHandler inject(List<CliToken> tokens) {
        List<String> args = StdoutHandler.parseArgs(tokens, NAME);
        CommandLine commandLine = CLIs.create(NAME)
                .addOption(new Option().setShortName("l").setFlag(true))
                .parse(args);
        boolean lineMode = commandLine.isFlagEnabled("l");
        return new WordCountHandler(lineMode);
    }

    private WordCountHandler(boolean lineMode) {
        this.lineMode = lineMode;
    }

    @Override
    /** 行模式下按换行分割累加行数；非 -l 模式返回提示信息 */
    public String apply(String input) {
        if (!this.lineMode) {
            // TODO 默认行为应对齐 `wc -l -w -c`，当前仅实现 -l
            // TODO the default behavior should be equivalent to `wc -l -w -c`
            result = "wc currently only support wc -l!\n";
        } else {
            if (input != null && !"".equals(input.trim())) {
                total.getAndAdd(input.split("\n").length);
            }
        }

        return null;
    }

    @Override
    /** @return 累计行数加换行，或错误提示文本 */
    public String result() {
        if (result != null) {
            return result;
        }

        return total.get() + "\n";
    }
}
