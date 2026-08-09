package com.taobao.arthas.core.util;

/**
 * 相等性与成员判定工具。
 * Created by vlinux on 15/5/19.
 */
public class ArthasCheckUtils {

    /**
     * 判断元素是否出现在可变参数数组中。
     *
     * @param e   待查元素
     * @param s   候选数组
     * @param <E> 元素类型
     * @return 存在相等项（含双 null）为 true<br/>
     * (1,1,2,3)        == true<br/>
     * (1,2,3,4)        == false<br/>
     * (null,1,null,2)  == true<br/>
     * (1,null)         == false
     */
    public static <E> boolean isIn(E e, E... s) {

        if (null != s) {
            for (E es : s) {
                if (isEquals(e, es)) {
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * 判断两元素是否相等（双 null 视为相等）。
     *
     * @param src    源元素
     * @param target 目标元素
     * @param <E>    元素类型
     * @return 相等为 true<br/>
     * (null, null)    == true<br/>
     * (1L,2L)         == false<br/>
     * (1L,1L)         == true<br/>
     * ("abc",null)    == false<br/>
     * (null,"abc")    == false
     */
    public static <E> boolean isEquals(E src, E target) {

        return null == src
                && null == target
                || null != src
                && null != target
                && src.equals(target);

    }
}
