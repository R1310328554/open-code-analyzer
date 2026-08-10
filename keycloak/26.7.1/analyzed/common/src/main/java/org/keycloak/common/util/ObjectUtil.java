/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.util;

/**
 * 通用对象/字符串比较与格式化小工具。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ObjectUtil {

    private ObjectUtil() {}

    /**
     * 两对象均为 null 或 {@link Object#equals} 为 true 时返回 true。
     *
     * @param str1 第一个对象
     * @param str2 第二个对象
     * @return 相等或双 null 时为 true
     */
    public static boolean isEqualOrBothNull(Object str1, Object str2) {
        if (str1 == null && str2 == null) {
            return true;
        }

        if ((str1 != null && str2 == null) || (str1 == null && str2 != null)) {
            return false;
        }

        return str1.equals(str2);
    }


    /** 将字符串首字母大写（假定非空）。 */
    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    /**
     * 摘自 Apache Commons {@code StringUtils}。
     *
     * <p>判断字符序列是否为 null、空串或仅空白。</p>
     *
     * <pre>
     * ObjectUtil.isBlank(null)      = true
     * ObjectUtil.isBlank("")        = true
     * ObjectUtil.isBlank(" ")       = true
     * ObjectUtil.isBlank("bob")     = false
     * ObjectUtil.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs 待检字符序列
     * @return 为 null、空或全空白时为 {@code true}
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
