package com.taobao.arthas.core.util;

import com.taobao.arthas.core.shell.term.impl.http.session.LRUCache;
import java.util.regex.Pattern;

/**
 * 正则表达式编译结果缓存管理器（单例）。
 * <p>
 * 使用 {@link LRUCache} 缓存 {@link Pattern} 实例，避免 watch/trace 等命令
 * 对同一表达式重复 {@link Pattern#compile(String)} 的开销；容量上限 {@value #MAX_CACHE_SIZE}。
 */
public class RegexCacheManager {
    private static final RegexCacheManager INSTANCE = new RegexCacheManager();
    
    /** LRU 缓存：正则字符串 → 已编译 Pattern */
    private final LRUCache<String, Pattern> regexCache;
    
    /** 最大缓存条目数 */
    private static final int MAX_CACHE_SIZE = 100;
    
    private RegexCacheManager() {
        this.regexCache = new LRUCache<>(MAX_CACHE_SIZE);
    }

    /** @return 全局单例 */
    public static RegexCacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取与 {@code regex} 对应的 Pattern：命中缓存则直接返回，否则编译并写入 LRU。
     * <p>
     * 无效正则时不捕获 {@code PatternSyntaxException}，便于调用方尽早发现配置错误。
     *
     * @param regex 正则表达式字符串
     * @return 编译后的 Pattern；regex 为 null 时返回 null
     */
    public Pattern getPattern(String regex) {
        if (regex == null) {
            return null;
        }
        
        Pattern pattern = regexCache.get(regex);
        if (pattern != null) {
            return pattern;
        }

        pattern = Pattern.compile(regex);
        regexCache.put(regex, pattern);
        
        return pattern;
    }
    
    /** 清空全部缓存条目 */
    public void clearCache() {
        regexCache.clear();
    }
    
    /** @return 当前缓存中已使用的条目数 */
    public int getCacheSize() {
        return regexCache.usedEntries();
    }

}
