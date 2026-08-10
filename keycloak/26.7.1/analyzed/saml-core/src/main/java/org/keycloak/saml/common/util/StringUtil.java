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
package org.keycloak.saml.common.util;

import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;


/**
 * 字符串判空与相等性校验工具。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 21, 2009
 */
public class StringUtil {

    /** 日志实例。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * 判断字符串非 null 且 trim 后非空。
     *
     * @param str 待检查字符串
     *
     * @return 非空返回 true
     */
    public static boolean isNotNull(String str) {
        return str != null && !"".equals(str.trim());
    }

    /**
     * 判断字符串为 null 或长度为 0（不 trim）。
     *
     * @param str 待检查字符串
     *
     * @return 为 null 或空串返回 true
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 断言两字符串相等，否则抛出 {@link RuntimeException}。
     *
     * @param first 期望值
     * @param second 实际值
     */
    public static void match(String first, String second) {
        if (!first.equals(second))
            throw logger.notEqualError(first, second);
    }
}