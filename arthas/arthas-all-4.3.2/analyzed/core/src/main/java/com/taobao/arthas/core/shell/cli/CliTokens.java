package com.taobao.arthas.core.shell.cli;

import com.taobao.arthas.core.shell.cli.impl.CliTokenImpl;

import java.util.List;

/**
 * CLI 词法 Token 工厂与分词入口。
 * <p>
 * 将用户输入拆分为 {@link CliToken} 序列，区分文本段与空白分隔符，
 * 供 Shell 解析命令、参数及 Tab 补全使用。
 *
 * @author beiwei30 on 09/11/2016.
 */
public class CliTokens {
    /**
     * 创建文本 Token（有效参数字段）。
     *
     * @param text 规范化后的文本内容
     * @return 文本 Token
     */
    public static CliToken createText(String text) {
        return new CliTokenImpl(true, text, text);
    }

    /**
     * 创建空白 Token（空格或制表符分隔段）。
     *
     * @param blank 空白字符序列
     * @return 空白 Token
     */
    public static CliToken createBlank(String blank) {
        return new CliTokenImpl(false, blank, blank);
    }

    /**
     * 对整行输入进行词法分词。
     * <p>
     * 委托 {@link CliTokenImpl#tokenize(String)}，支持引号转义与管道符修正。
     *
     * @param s 待分词的原始字符串
     * @return Token 列表
     */
    public static List<CliToken> tokenize(String s) {
        return CliTokenImpl.tokenize(s);
    }
}
