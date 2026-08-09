package com.taobao.arthas.core.shell.handlers.shell;

import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.impl.ShellImpl;

/**
 * Shell 关闭回调：终止 Job 控制器并等待关闭 Future 完成。
 * <p>
 * 终端 {@link com.taobao.arthas.core.shell.term.Term#close()} 或用户 exit 时触发，
 * 通过 {@link ShellImpl#jobController()} 清理前台/后台 Job 并通知 {@link ShellImpl#closedFutureHandler()}。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class CloseHandler implements Handler<Void> {
    /** 即将关闭的 Shell 会话 */
    private ShellImpl shell;

    /** @param shell 待关闭的 Shell 实例 */
    public CloseHandler(ShellImpl shell) {
        this.shell = shell;
    }

    @Override
    /** 关闭 Job 控制器并在全部 Job 结束后完成 closedFuture */
    public void handle(Void event) {
        shell.jobController().close(shell.closedFutureHandler());
    }
}
