package com.taobao.arthas.core.util.matcher;

/**
 * 恒为 false 的占位匹配器，用于禁用某路过滤条件。
 * @author ralf0131 2017-01-06 13:33.
 */
public class FalseMatcher<T> implements Matcher<T> {

    /**
     * 对任意 target 均不匹配。
     *
     * @param target 待测对象
     * @return 恒为 false
     */
    @Override
    public boolean matching(T target) {
        return false;
    }
}
