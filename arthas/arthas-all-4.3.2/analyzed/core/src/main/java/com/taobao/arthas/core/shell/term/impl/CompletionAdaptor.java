package com.taobao.arthas.core.shell.term.impl;

import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.cli.Completion;
import com.taobao.arthas.core.shell.cli.CompletionUtils;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 将 Arthas {@link Completion} 适配到 termd readline 补全 API 的桥接类。
 * <p>
 * 负责把候选列表转为 code point 建议，或计算最长公共前缀后 partial complete。
 *
 * @author beiwei30 on 23/11/2016.
 */
class CompletionAdaptor implements Completion {
    /** 当前 Shell 会话 */
    private final Session session;
    /** readline 当前整行文本 */
    private final String line;
    /** 行 token 化结果 */
    private final List<CliToken> tokens;
    /** 底层 termd readline 补全对象 */
    private final io.termd.core.readline.Completion completion;

    /**
     * @param line 原始输入行
     * @param tokens CLI token 列表
     * @param completion termd 补全对象
     * @param session Shell 会话
     */
    public CompletionAdaptor(String line, List<CliToken> tokens, io.termd.core.readline.Completion completion,
                             Session session) {
        this.line = line;
        this.tokens = tokens;
        this.completion = completion;
        this.session = session;
    }

    @Override
    /** @return 关联 Shell 会话 */
    public Session session() {
        return session;
    }

    @Override
    /** @return 原始输入行 */
    public String rawLine() {
        return line;
    }

    @Override
    /** @return 行 token 列表 */
    public List<CliToken> lineTokens() {
        return tokens;
    }

    @Override
    /** 多候选补全：优先最长公共前缀，否则列出全部建议 */
    public void complete(List<String> candidates) {
        String lastToken = tokens.isEmpty() ? null : tokens.get(tokens.size() - 1).value();
        if(StringUtils.isBlank(lastToken)) {
            lastToken = "";
        }
        if (candidates.size() > 1) {
            // 多个候选时尝试补全公共前缀
            String commonPrefix = CompletionUtils.findLongestCommonPrefix(candidates);
            if (commonPrefix.length() > 0) {
                if (!commonPrefix.equals(lastToken)) {
                    // 仅当公共前缀长于当前 token 时才 partial complete
                    if (commonPrefix.length() > lastToken.length()) {
                        String strToComplete = commonPrefix.substring(lastToken.length());
                        completion.complete(io.termd.core.util.Helper.toCodePoints(strToComplete), false);
                        return;
                    }
                }
            }
        }
        if (candidates.size() > 0) {
            List<int[]> suggestions = new LinkedList<int[]>();
            for (String candidate : candidates) {
                suggestions.add(io.termd.core.util.Helper.toCodePoints(candidate));
            }
            completion.suggest(suggestions);
        } else {
            completion.end();
        }
    }

    @Override
    /** 单片段补全，直接转发给 termd completion */
    public void complete(String value, boolean terminal) {
        completion.complete(io.termd.core.util.Helper.toCodePoints(value), terminal);
    }
}
