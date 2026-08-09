package com.taobao.arthas.core.util.matcher;

/**
 * 恒为 true 的匹配器，用于「不过滤、匹配全部目标」的场景。
 *
 * @author ralf0131 2017-01-06 13:48.
 */
public final class TrueMatcher<T> implements Matcher<T> {

    /**
     * 对任意目标均返回 true。
     *
     * @param target 待匹配对象（本实现中忽略其值）
     * @return 始终为 true
     */
    @Override
    public boolean matching(T target) {
        return true;
    }

}
