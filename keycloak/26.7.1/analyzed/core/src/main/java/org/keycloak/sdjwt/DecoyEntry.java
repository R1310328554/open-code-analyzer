/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.sdjwt;

import java.util.Objects;

import org.keycloak.jose.jws.crypto.HashUtils;

/**
 * 诱饵条目的抽象基类，根据给定盐值生成披露摘要哈希。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 *
 */
public abstract class DecoyEntry extends DisclosureSpec.DisclosureData {

    /** @param salt 非空盐值，用于计算诱饵摘要 */
    protected DecoyEntry(SdJwtSalt salt) {
        super(Objects.requireNonNull(salt, "DecoyEntry always requires a non null salt"));
    }

    /**
     * 根据指定哈希算法计算盐值的披露摘要。
     *
     * @param hashAlg 哈希算法名称
     * @return Base64Url 编码的摘要字符串
     */
    public String getDisclosureDigest(String hashAlg) {
        return SdJwtUtils.encodeNoPad(HashUtils.hash(hashAlg, salt.toString().getBytes()));
    }
}
