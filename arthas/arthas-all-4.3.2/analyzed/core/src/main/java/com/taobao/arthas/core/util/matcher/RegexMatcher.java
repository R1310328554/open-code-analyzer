package com.taobao.arthas.core.util.matcher;

import com.taobao.arthas.core.util.RegexCacheManager;
import java.util.regex.Pattern;

/**
 * 正则表达式匹配器：对目标字符串做完整匹配（{@link Pattern#matches()} 语义）。
 * <p>
 * 首次调用 {@link #matching} 时通过 {@link RegexCacheManager} 懒编译并缓存 {@link Pattern}，
 * 避免 watch/trace 等命令对同一表达式重复编译。
 * </p>
 *
 * @author ralf0131 2017-01-06 13:16.
 */
public class RegexMatcher implements Matcher<String> {

    /** 正则表达式源字符串 */
    private final String pattern;
    /** 首次匹配时编译得到的 Pattern，volatile 保证多线程可见性 */
    private volatile Pattern compiledPattern;

    /**
     * @param pattern 正则表达式字符串
     */
    public RegexMatcher(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 判断目标字符串是否与当前正则完整匹配。
     *
     * @param target 待匹配字符串
     * @return 匹配成功返回 true；{@code target} 或 {@code pattern} 为 null 时返回 false
     */
    @Override
    public boolean matching(String target) {
        if (null == target || null == pattern) {
            return false;
        }

        // 在第一次 matching 时才编译正则表达式
        if (compiledPattern == null) {
            compiledPattern = RegexCacheManager.getInstance().getPattern(pattern);
        }
        
        return compiledPattern != null && compiledPattern.matcher(target).matches();
    }
}
