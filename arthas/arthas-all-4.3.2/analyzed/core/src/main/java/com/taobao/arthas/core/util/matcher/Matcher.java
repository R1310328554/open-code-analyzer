package com.taobao.arthas.core.util.matcher;

/**
 * 泛型匹配器函数式接口，供类名/方法名等条件过滤。
 * Created by vlinux on 15/5/17.
 */
public interface Matcher<T> {

    /**
     * 判断目标是否满足匹配规则。
     *
     * @param target 待测目标（通常为类名或方法名字符串）
     * @return 匹配成功返回 true
     */
    boolean matching(T target);

}
