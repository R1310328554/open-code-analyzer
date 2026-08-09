package com.taobao.arthas.core.shell.cli;

import com.taobao.arthas.core.shell.session.Session;

import java.util.List;

/**
 * Shell 命令行 Tab 补全上下文。
 * <p>
 * 提供当前会话、原始输入行及已分词 Token，命令实现通过
 * {@link #complete(List)} 或 {@link #complete(String, boolean)} 回传候选。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface Completion {

    /**
     * @return 当前 Shell 会话，可用于文件路径补全等场景读取工作目录等上下文
     */
    Session session();

    /**
     * @return 待补全的原始行文本（未做转义展开）
     */
    String rawLine();

    /**
     * @return 当前行已分词后的 Token 列表
     */
    List<CliToken> lineTokens();

    /**
     * 以候选列表结束补全，Shell 将在终端展示供用户选择。
     *
     * @param candidates 补全候选项
     */
    void complete(List<String> candidates);

    /**
     * 以单个插入片段结束补全。
     *
     * @param value 要插入到光标处的文本
     * @param terminal 是否为终结补全（false 表示仍可继续 Tab 展开）
     */
    void complete(String value, boolean terminal);

}
