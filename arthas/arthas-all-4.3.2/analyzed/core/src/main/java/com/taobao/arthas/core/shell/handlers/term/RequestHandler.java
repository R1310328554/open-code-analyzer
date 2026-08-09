package com.taobao.arthas.core.shell.handlers.term;

import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.term.impl.TermImpl;
import io.termd.core.function.Consumer;

/**
 * readline 行提交 Handler：用户按 Enter 后将整行交给 Shell 行处理器。
 * <p>
 * 清除 {@link TermImpl} 的 inReadline 标志后调用 {@link com.taobao.arthas.core.shell.handlers.shell.ShellLineHandler}，
 * 解析 exit/jobs/fg/bg/kill 等内置命令或创建 Arthas 诊断 Job。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class RequestHandler implements Consumer<String> {
    /** 关联 Term，用于更新 readline 状态 */
    private TermImpl term;
    /** 处理完整输入行的 Shell Handler（通常为 ShellLineHandler） */
    private final Handler<String> lineHandler;

    /**
     * @param term 当前终端实例
     * @param lineHandler 接收一行文本的 Shell 处理器
     */
    public RequestHandler(TermImpl term, Handler<String> lineHandler) {
        this.term = term;
        this.lineHandler = lineHandler;
    }

    @Override
    /** 一行输入完成：退出 readline 模式并分派给 lineHandler */
    public void accept(String line) {
        term.setInReadline(false);
        lineHandler.handle(line);
    }
}
