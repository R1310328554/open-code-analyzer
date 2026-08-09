package com.taobao.arthas.core.util.matcher;

import com.taobao.arthas.core.util.ArthasCheckUtils;

/**
 * 基于 {@link com.taobao.arthas.core.util.ArthasCheckUtils#isEquals} 的全等匹配器。
 * <p>目标与构造时给定 pattern 相等（含 null 安全）时返回 true。</p>
 * @author ralf0131 2017-01-06 13:18.
 */
public class EqualsMatcher<T> implements Matcher<T> {

    /** 期望完全相等的模式值。 */
    private final T pattern;

    /** @param pattern 全等比较的目标值 */
    public EqualsMatcher(T pattern) {
        this.pattern = pattern;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matching(T target) {
        return ArthasCheckUtils.isEquals(target, pattern);
    }
}
