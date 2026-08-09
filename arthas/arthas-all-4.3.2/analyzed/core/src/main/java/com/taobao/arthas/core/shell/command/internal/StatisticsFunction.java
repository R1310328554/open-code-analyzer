package com.taobao.arthas.core.shell.command.internal;

import io.termd.core.function.Function;

/**
 * 管道末端统计型 {@link Function} 扩展接口。
 * <p>
 * {@link #apply} 消费上游输出并累积状态；Job 结束时调用 {@link #result()} 输出汇总
 *（如 grep -c 行数、wc -l 行数）。
 *
 * @author diecui1202 on 2017/10/24.
 */
public interface StatisticsFunction extends Function<String, String> {

    /** @return 统计完成后的单行或多行结果文本 */
    String result();
}
