package com.taobao.arthas.core.shell.handlers.server;

import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.impl.ShellImpl;
import com.taobao.arthas.core.shell.impl.ShellServerImpl;

/**
 * Shell 会话关闭回调：从 {@link ShellServerImpl} 注销已断开的 {@link ShellImpl}。
 * <p>
 * 客户端断开 Telnet/HTTP 连接时触发，便于服务端清理会话表与资源。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class SessionClosedHandler implements Handler<Future<Void>> {
    private ShellServerImpl shellServer;
    private final ShellImpl session;

    /** @param shellServer 所属 Shell 服务端
     *  @param session 即将移除的 Shell 会话实例 */
    public SessionClosedHandler(ShellServerImpl shellServer, ShellImpl session) {
        this.shellServer = shellServer;
        this.session = session;
    }

    @Override
    /** 从服务端会话集合中移除已关闭的 Shell */
    public void handle(Future<Void> ar) {
        shellServer.removeSession(session);
    }
}
