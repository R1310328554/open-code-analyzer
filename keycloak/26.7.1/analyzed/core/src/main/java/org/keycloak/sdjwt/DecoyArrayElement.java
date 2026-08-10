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

import com.fasterxml.jackson.databind.JsonNode;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_SD_UNDISCLOSED_ARRAY;

/**
 * 数组诱饵元素，在 SD-JWT 数组声明的指定位置插入虚假未披露占位符以混淆元数据。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class DecoyArrayElement extends DecoyEntry {

    /** 诱饵元素在数组中的插入位置。 */
    private final Integer index;

    private DecoyArrayElement(SdJwtSalt salt, Integer index) {
        super(salt);
        this.index = index;
    }

    /**
     * 返回在签发者 JWT 中可见的数组元素值（未披露数组占位符）。
     *
     * @param hashAlg 哈希算法名称
     */
    public JsonNode getVisibleValue(String hashAlg) {
        return SdJwtUtils.mapper.createObjectNode().put(CLAIM_NAME_SD_UNDISCLOSED_ARRAY, getDisclosureDigest(hashAlg));
    }

    /** @return 诱饵元素的目标索引位置 */
    public Integer getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DecoyArrayElement)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        DecoyArrayElement that = (DecoyArrayElement) o;
        return Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(index);
        return result;
    }

    /** 构建 {@link DecoyArrayElement} 的建造者。 */
    public static class Builder {
        private SdJwtSalt salt;
        private Integer index;

        /** @param salt 用于生成摘要的盐值 */
        public Builder withSalt(SdJwtSalt salt) {
            this.salt = salt;
            return this;
        }

        /** @param index 数组中的插入位置 */
        public Builder atIndex(Integer index) {
            this.index = index;
            return this;
        }

        /** @return 构建完成的诱饵数组元素 */
        public DecoyArrayElement build() {
            salt = salt == null ? new SdJwtSalt(SdJwtUtils.randomSalt()) : salt;
            return new DecoyArrayElement(salt, index);
        }
    }

    /** @return 新的建造者实例 */
    public static Builder builder() {
        return new Builder();
    }
}
