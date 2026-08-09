package com.taobao.arthas.core.shell.system.impl;

import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.session.Session;

import java.util.List;

/**
 * 命令级补全的 {@link Completion} 装饰器：剥离命令名后，将剩余 token 与行片段转发给具体命令。
 * <p>
 * 由 {@link InternalCommandManager#complete} 在定位到目标命令后构造，
 * 使各 Command 只需处理自身参数段的补全逻辑。
 *
 * @author beiwei30 on 23/11/2016.
 */
class CommandCompletion implements Completion {
    /** 外层 Shell 补全上下文，最终候选写入此对象 */
    private final Completion completion;
    /** 命令名之后的原始行片段（供命令解析参数） */
    private final String line;
    /** 命令名之后的 CLI token 列表 */
    private final List<CliToken> newTokens;

    /**
     * @param completion 外层补全对象
     * @param line 命令名之后的行文本
     * @param newTokens 命令参数 token 列表
     */
    public CommandCompletion(Completion completion, String line, List<CliToken> newTokens) {
        this.completion = completion;
        this.line = line;
        this.newTokens = newTokens;
    }

    @Override
    /** @return 当前 Shell 会话 */
    public Session session() {
        return completion.session();
    }

    @Override
    /** @return 命令参数段的原始行文本 */
    public String rawLine() {
        return line;
    }

    @Override
    /** @return 命令参数 token 列表 */
    public List<CliToken> lineTokens() {
        return newTokens;
    }

    @Override
    /** 将多个候选词转发给外层补全 */
    public void complete(List<String> candidates) {
        completion.complete(candidates);
    }

    @Override
    /** 将单个补全片段转发给外层，terminal 表示是否结束补全 */
    public void complete(String value, boolean terminal) {
        completion.complete(value, terminal);
    }
}
