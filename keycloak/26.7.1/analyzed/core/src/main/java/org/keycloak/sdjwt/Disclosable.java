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

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 可披露对象的抽象基类，处理未披露声明与数组元素，
 * 提供从 Base64Url 编码字符串生成披露摘要的功能。
 * <p>
 * 隐藏声明与数组元素的方式是在签名的可验证凭证中以摘要替代明文。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 *
 */
public abstract class Disclosable {
    /** 用于生成披露字符串的随机盐值。 */
    private final SdJwtSalt salt;

    /**
     * 返回未披露值的数组表示，用于编码（披露字符串）与哈希（VC 中的 {@code _sd} 摘要数组）。
     */
    abstract Object[] toArray();

    /** @param salt 非空盐值 */
    protected Disclosable(SdJwtSalt salt) {
        this.salt = Objects.requireNonNull(salt, "Disclosure always requires a salt must not be null");
    }

    /** @return 盐值对象 */
    public SdJwtSalt getSalt() {
        return salt;
    }

    /** @return 盐值的字符串形式 */
    public String getSaltAsString() {
        return salt.toString();
    }

    /** @return 未披露值的 JSON 数组字符串 */
    public String toJson() {
        try {
            return SdJwtUtils.printJsonArray(toArray());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /** @return Base64Url 编码的披露字符串 */
    public String getDisclosureString() {
        String json = toJson();
        return SdJwtUtils.encodeNoPad(json);
    }

    /**
     * 计算披露字符串的哈希摘要。
     *
     * @param hashAlg 哈希算法名称
     */
    public String getDisclosureDigest(String hashAlg) {
        return SdJwtUtils.hashAndBase64EncodeNoPad(getDisclosureString(), hashAlg);
    }

    @Override
    public String toString() {
        return getDisclosureString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Disclosable)) {
            return false;
        }

        Disclosable that = (Disclosable) o;
        return salt.equals(that.salt);
    }

    @Override
    public int hashCode() {
        return salt.hashCode();
    }
}
