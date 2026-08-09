package com.taobao.arthas.core.util.matcher;

/**
 * 通配符匹配器，支持 {@code *}（任意长度）与 {@code ?}（单字符），反斜杠可转义特殊字符。
 *
 * @author ralf0131 2017-01-06 13:17.
 */
public class WildcardMatcher implements Matcher<String> {

    /** 通配符模式字符串 */
    private final String pattern;

    /** 星号通配符，匹配零个或多个任意字符 */
    private static final Character ASTERISK = '*';
    /** 问号通配符，匹配恰好一个任意字符 */
    private static final Character QUESTION_MARK = '?';
    /** 转义符，使下一个字符按字面量匹配 */
    private static final Character ESCAPE = '\\';



    /**
     * @param pattern 通配符表达式
     */
    public WildcardMatcher(String pattern) {
        this.pattern = pattern;
    }


    /**
     * 判断目标字符串是否与模式匹配。
     *
     * @param target 待匹配字符串
     * @return 匹配成功返回 true
     */
    @Override
    public boolean matching(String target) {
        return match(target, pattern, 0, 0);
    }

    /**
     * 递归匹配核心逻辑：从 {@code stringStartNdx} 与 {@code patternStartNdx} 起同步扫描。
     */
    private boolean match(String target, String pattern, int stringStartNdx, int patternStartNdx) {
        //#135
        if(target==null || pattern==null){
            return false;
        }
        int pNdx = patternStartNdx;
        int sNdx = stringStartNdx;
        int pLen = pattern.length();
        if (pLen == 1) {
            // 单字符模式为 * 时可直接匹配任意目标
            if (pattern.charAt(0) == ASTERISK) {
                return true;
            }
        }
        int sLen = target.length();
        boolean nextIsNotWildcard = false;

        while (true) {

            // 目标串已扫描完毕
            if ((sNdx >= sLen)) {
                // 末尾仍可消费模式中的连续 *
                while ((pNdx < pLen) && (pattern.charAt(pNdx) == ASTERISK)) {
                    pNdx++;
                }
                return pNdx >= pLen;
            }
            // 模式已结束但目标串仍有剩余
            if (pNdx >= pLen) {
                return false;
            }
            // 当前模式字符
            char p = pattern.charAt(pNdx);

            // 通配符分支处理
            if (!nextIsNotWildcard) {

                if (p == ESCAPE) {
                    pNdx++;
                    nextIsNotWildcard = true;
                    continue;
                }
                if (p == QUESTION_MARK) {
                    sNdx++;
                    pNdx++;
                    continue;
                }
                if (p == ASTERISK) {
                    // 查看 * 后的下一个模式字符
                    char pnext = 0;
                    if (pNdx + 1 < pLen) {
                        pnext = pattern.charAt(pNdx + 1);
                    }
                    // 连续 ** 等价于单个 *
                    if (pnext == ASTERISK) {
                        pNdx++;
                        continue;
                    }
                    int i;
                    pNdx++;

                    // 从目标串当前位置到末尾，递归尝试匹配剩余模式
                    for (i = target.length(); i >= sNdx; i--) {
                        if (match(target, pattern, i, pNdx)) {
                            return true;
                        }
                    }
                    return false;
                }
            } else {
                nextIsNotWildcard = false;
            }

            // 普通字符需与目标当前位置一致
            if (p != target.charAt(sNdx)) {
                return false;
            }

            // 当前字符匹配，继续推进
            sNdx++;
            pNdx++;
        }
    }
}
