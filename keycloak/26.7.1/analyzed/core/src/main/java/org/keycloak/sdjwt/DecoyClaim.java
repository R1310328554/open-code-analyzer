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

/**
 * 诱饵声明（Decoy Claim），在 SD-JWT 载荷的 {@code _sd} 数组中插入虚假摘要以混淆元数据。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class DecoyClaim extends DecoyEntry {

    private DecoyClaim(SdJwtSalt salt) {
        super(salt);
    }

    /** 构建 {@link DecoyClaim} 实例的建造者。 */
    public static class Builder {
        private SdJwtSalt salt;

        /** @param salt 用于生成摘要的盐值 */
        public Builder withSalt(SdJwtSalt salt) {
            this.salt = salt;
            return this;
        }

        /** @return 构建完成的诱饵声明 */
        public DecoyClaim build() {
            salt = salt == null ? new SdJwtSalt(SdJwtUtils.randomSalt()) : salt;
            return new DecoyClaim(salt);
        }
    }

    /** @return 新的建造者实例 */
    public static Builder builder() {
        return new Builder();
    }
}
