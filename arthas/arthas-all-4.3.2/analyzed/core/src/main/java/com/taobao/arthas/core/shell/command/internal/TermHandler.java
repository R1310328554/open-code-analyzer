package com.taobao.arthas.core.shell.command.internal;

import com.taobao.arthas.core.shell.term.Term;

/**
 * 将管道数据直接写入 Shell {@link Term} 终端。
 * <p>
 * 用于命令结果回显到 Telnet/HTTP 客户端；{@link #apply} 写终端后仍返回原 data。
 * 
 * @author gehui 2017年7月26日 上午11:20:00
 */
public class TermHandler extends StdoutHandler {
    private Term term;

    /** @param term 当前会话绑定的终端实例 */
    public TermHandler(Term term) {
        this.term = term;
    }

    @Override
    /** 将 data 写入终端并原样返回，供统计型 Handler 继续消费 */
    public String apply(String data) {
        term.write(data);
        return data;
    }
}