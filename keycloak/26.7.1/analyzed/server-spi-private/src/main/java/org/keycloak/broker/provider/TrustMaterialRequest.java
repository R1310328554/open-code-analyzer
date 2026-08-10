/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.broker.provider;

/**
 * 信任材料解析请求：携带 kid、算法与 issuer 等查询条件。
 */
public class TrustMaterialRequest {

    private final String kid;
    private final String algorithm;
    private final String issuer;

    /** 通过 {@link Builder} 构造不可变请求对象。 */
    private TrustMaterialRequest(Builder builder) {
        this.kid = builder.kid;
        this.algorithm = builder.algorithm;
        this.issuer = builder.issuer;
    }

    /** 密钥 ID（kid）。 */
    public String getKid() {
        return kid;
    }

    /** 期望的签名/密钥算法。 */
    public String getAlgorithm() {
        return algorithm;
    }

    /** 断言或密钥的 issuer。 */
    public String getIssuer() {
        return issuer;
    }

    /** 创建请求构建器。 */
    public static Builder builder() {
        return new Builder();
    }

    /** {@link TrustMaterialRequest} 的流式构建器。 */
    public static class Builder {

        private String kid;
        private String algorithm;
        private String issuer;

        /** 设置 kid。 */
        public Builder kid(String kid) {
            this.kid = kid;
            return this;
        }

        /** 设置算法。 */
        public Builder algorithm(String algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        /** 设置 issuer。 */
        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        /** 构建不可变 {@link TrustMaterialRequest}。 */
        public TrustMaterialRequest build() {
            return new TrustMaterialRequest(this);
        }
    }
}
