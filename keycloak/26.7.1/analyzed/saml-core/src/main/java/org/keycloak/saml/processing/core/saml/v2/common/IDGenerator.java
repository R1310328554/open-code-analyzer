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
package org.keycloak.saml.processing.core.saml.v2.common;

import java.util.UUID;

import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;

/**
 * 生成 SAML 消息唯一标识符的工具类。
 * <p>基于 {@link UUID} 生成符合 SAML 规范的 ID 字符串。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 5, 2009
 */
public class IDGenerator {

    /** 日志记录器。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * 创建不带前缀的唯一 ID。
     *
     * @return UUID 字符串
     */
    public static String create() {
        return UUID.randomUUID().toString();
    }

    /**
     * 创建带指定前缀的唯一 ID。
     *
     * @param prefix 前缀字符串，不可为 null
     *
     * @return 前缀与 UUID 拼接后的 ID
     *
     * @throws IllegalArgumentException 当 prefix 为 null 时抛出
     */
    public static String create(String prefix) {
        if (prefix == null)
            throw logger.nullArgumentError("prefix");
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(IDGenerator.create());
        return sb.toString();
    }
}