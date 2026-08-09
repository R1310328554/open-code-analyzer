package com.taobao.arthas.core.util;

import java.util.List;

import com.taobao.arthas.core.shell.cli.CliToken;

/**
 * Shell 命令词法分析后的 {@link CliToken} 列表辅助工具。
 * <p>用于 Tab 补全、参数回溯等场景定位文本 token。</p>
 *
 * @author gehui 2017-07-27 11:39:56
 */
public class TokenUtils {

    /**
     * 返回第一个文本类型 token
     */
    public static CliToken findFirstTextToken(List<CliToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        CliToken first = null;
        for (CliToken token : tokens) {
            if (token != null && token.isText()) {
                first = token;
                break;
            }
        }
        return first;
    }

    /**
     * 返回最后一个文本类型 token
     */
    public static CliToken findLastTextToken(List<CliToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        //#165
        for (int i = tokens.size() - 1; i >= 0; i--) {
            CliToken token = tokens.get(i);
            if (token != null && token.isText()) {
                return token;
            }
        }
        return null;
    }

    /**
     * 返回第二个文本 token 的字符串值
     */
    public static String findSecondTokenText(List<CliToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        boolean first = true;
        for (CliToken token : tokens) {
            if (token != null && token.isText()) {
                if (first) {
                    first = false;
                } else {
                    return token.value();
                }
            }
        }
        return null;
    }

    /** 返回 token 列表最后一个元素（任意类型）。 */
    public static CliToken getLast(List<CliToken> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return null;
        } else {
            return tokens.get(tokens.size() -1);
        }
    }

    /**
     * Tab 补全时根据当前末尾 token 推断「上一个完整参数」。
     * <p>例如补全方法名时回退到类名。</p>
     */
    public static String retrievePreviousArg(List<CliToken> tokens, String lastToken) {
        if (StringUtils.isBlank(lastToken) && tokens.size() > 2) {
            // 末尾为空格：上一参数为 class 名
            return tokens.get(tokens.size() - 2).value();
        } else if (tokens.size() > 3) {
            // 正在输入方法名：回退到 class 名
            return tokens.get(tokens.size() - 3).value();
        } else {
            return Constants.EMPTY_STRING;
        }
    }
}
