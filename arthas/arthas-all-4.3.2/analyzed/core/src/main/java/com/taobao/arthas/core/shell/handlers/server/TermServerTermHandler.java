package com.taobao.arthas.core.shell.handlers.server;

import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.impl.ShellServerImpl;
import com.taobao.arthas.core.shell.term.Term;

/**
 * TermServer 新连接回调：将客户端 {@link Term} 交给 Shell 服务端处理。
 * <p>
 * 每个 Telnet/HTTP 连接建立后 TermServer 产生 Term 实例，
 * 本 Handler 委托 {@link ShellServerImpl#handleTerm} 创建 Shell 会话并注册 readline。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class TermServerTermHandler implements Handler<Term> {
    /** 接收新 Term 并创建 Shell 会话的服务端 */
    private ShellServerImpl shellServer;

    /** @param shellServer 所属 Shell 服务端 */
    public TermServerTermHandler(ShellServerImpl shellServer) {
        this.shellServer = shellServer;
    }

    @Override
    /** 将新连接的 Term 转交 ShellServer 建立会话 */
    public void handle(Term term) {
        shellServer.handleTerm(term);
    }
}
