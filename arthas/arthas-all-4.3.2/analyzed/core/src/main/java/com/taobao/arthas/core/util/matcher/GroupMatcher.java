package com.taobao.arthas.core.util.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * 可组合多个子 {@link Matcher} 的组匹配器接口。
 * <p>提供 {@link And} 与 {@link Or} 两种逻辑组合实现。</p>
 * @author ralf0131 2017-01-06 13:29.
 */
public interface GroupMatcher<T> extends Matcher<T> {

    /**
     * 追加匹配器
     *
     * @param matcher 匹配器
     */
    void add(Matcher<T> matcher);

    /**
     * 与关系组匹配
     *
     * @param <T> 匹配类型
     */
    class And<T> implements GroupMatcher<T> {

        private final Collection<Matcher<T>> matchers;

        /**
         * 与关系组匹配构造<br/>
         * 当且仅当目标符合匹配组的所有条件时才判定匹配成功
         *
         * @param matchers 待进行与关系组匹配的匹配集合
         */
        public And(Matcher<T>... matchers) {
            this.matchers = Arrays.asList(matchers);
        }

        /** 全部子匹配器成功才返回 true。 */
        @Override
        public boolean matching(T target) {
            for (Matcher<T> matcher : matchers) {
                if (!matcher.matching(target)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void add(Matcher<T> matcher) {
            matchers.add(matcher);
        }
    }

    /**
     * 或关系组匹配
     *
     * @param <T> 匹配类型
     */
    class Or<T> implements GroupMatcher<T> {

        private final Collection<Matcher<T>> matchers;

        /** 空 OR 组，可通过 {@link #add} 动态追加。 */
        public Or() {
            this.matchers = new ArrayList<Matcher<T>>();
        }

        /**
         * 或关系组匹配构造<br/>
         * 当且仅当目标符合匹配组的任一条件时就判定匹配成功
         *
         * @param matchers 待进行或关系组匹配的匹配集合
         */
        public Or(Matcher<T>... matchers) {
            this.matchers = Arrays.asList(matchers);
        }

        /** 使用已有匹配器集合构造 OR 组。 */
        public Or(Collection<Matcher<T>> matchers) {
            this.matchers = matchers;
        }

        /** 任一子匹配器成功即返回 true。 */
        @Override
        public boolean matching(T target) {
            for (Matcher<T> matcher : matchers) {
                if (matcher.matching(target)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void add(Matcher<T> matcher) {
            matchers.add(matcher);
        }
    }

}
