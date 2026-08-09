package com.taobao.arthas.core.shell.term.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.cli.CliTokens;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;

import io.termd.core.function.Consumer;
import io.termd.core.readline.Completion;

import java.util.Collections;
import java.util.List;

/**
 * termd readline Tab 补全入口：将 code point 行转为 CLI token，再分派给 Shell 补全 handler。
 * <p>
 * 由 {@link TermImpl} 在 readline 时注册为 termd {@link Consumer}，
 * 内部构造 {@link CompletionAdaptor} 桥接到 {@link InternalCommandManager#complete}。
 *
 * @author beiwei30 on 23/11/2016.
 */
class CompletionHandler implements Consumer<Completion> {
    private static final Logger logger = LoggerFactory.getLogger(CompletionHandler.class);
    /** Shell 层补全 handler（通常调用 CommandManager） */
    private final Handler<com.taobao.arthas.core.shell.cli.Completion> completionHandler;
    /** 当前终端会话 */
    private final Session session;

    /**
     * @param completionHandler Arthas 补全处理器
     * @param session 当前 Shell 会话
     */
    public CompletionHandler(Handler<com.taobao.arthas.core.shell.cli.Completion> completionHandler, Session session) {
        this.completionHandler = completionHandler;
        this.session = session;
    }

    @Override
    /** termd 触发补全：tokenize 后包装为 CompletionAdaptor 并交给上层 */
    public void accept(final Completion completion) {
        try {
            final String line = io.termd.core.util.Helper.fromCodePoints(completion.line());
            final List<CliToken> tokens = Collections.unmodifiableList(CliTokens.tokenize(line));
            com.taobao.arthas.core.shell.cli.Completion comp = new CompletionAdaptor(line, tokens, completion, session);
            completionHandler.handle(comp);
        } catch (Throwable t) {
            // t.printStackTrace();
            logger.error("completion error", t);
        }
    }
}
