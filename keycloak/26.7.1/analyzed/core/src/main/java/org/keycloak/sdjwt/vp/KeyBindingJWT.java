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
package org.keycloak.sdjwt.vp;

import java.security.cert.Certificate;
import java.util.List;
import java.util.Optional;

import org.keycloak.OID4VCConstants;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.representations.IDToken;
import org.keycloak.sdjwt.JwsToken;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 密钥绑定 JWT（Key Binding JWT），用于将持有者密钥与 SD-JWT 展示绑定。
 *
 * <p>
 * 继承自 {@link JwsToken}，支持从 JWS 字符串解析或通过建造者构建并签名。
 * </p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 *
 */
public class KeyBindingJWT extends JwsToken {

    /**
     * 从 JWS 紧凑序列化字符串解析密钥绑定 JWT。
     *
     * @param jwsString JWS 紧凑序列化字符串
     */
    public KeyBindingJWT(String jwsString) {
        super(jwsString);
    }

    protected KeyBindingJWT(JWSHeader jwsHeader, ObjectNode payload, SignatureSignerContext signer) {
        super(jwsHeader, payload);
        getJwsHeader().setType(OID4VCConstants.KEYBINDING_JWT_TYP);
        Optional.ofNullable(signer).ifPresent(this::sign);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** 用于构建并签名 {@link KeyBindingJWT} 的建造者。 */
    public static class Builder {

        protected JWSHeader jwsHeader;

        protected ObjectNode payload;

        private SignatureSignerContext signerContext;

        public Builder() {
            this.jwsHeader = new JWSHeader();
            this.payload = JsonNodeFactory.instance.objectNode();
        }

        private JWSHeader getJwsHeader() {
            if (jwsHeader == null) {
                this.jwsHeader = new JWSHeader();
            }
            return jwsHeader;
        }

        private ObjectNode getPayload() {
            if (payload == null) {
                this.payload = JsonNodeFactory.instance.objectNode();
            }
            return payload;
        }

        /** 设置 JWS 头部。 */
        public Builder withJwsHeader(JWSHeader jwsHeader) {
            this.jwsHeader = jwsHeader;
            return this;
        }

        /** 设置 JWT 载荷。 */
        public Builder withPayload(ObjectNode payload) {
            this.payload = payload;
            return this;
        }

        /** 设置签发时间（iat）声明。 */
        public Builder withIat(long iat)
        {
            getPayload().put(OID4VCConstants.CLAIM_NAME_IAT, iat);
            return this;
        }

        /** 设置生效时间（nbf）声明。 */
        public Builder withNbf(long nbf)
        {
            getPayload().put(OID4VCConstants.CLAIM_NAME_NBF, nbf);
            return this;
        }

        /** 设置过期时间（exp）声明。 */
        public Builder withExp(long exp)
        {
            getPayload().put(OID4VCConstants.CLAIM_NAME_EXP, exp);
            return this;
        }

        /** 设置 nonce 声明，用于防重放。 */
        public Builder withNonce(String nonce)
        {
            getPayload().put(IDToken.NONCE, nonce);
            return this;
        }

        /** 设置受众（aud）声明。 */
        public Builder withAudience(String aud)
        {
            getPayload().put(IDToken.AUD, aud);
            return this;
        }

        /** 设置 JWS 头部的密钥 ID（kid）。 */
        public Builder withKid(String kid)
        {
            getJwsHeader().setKeyId(kid);
            return this;
        }

        /** 设置 X.509 证书链（x5c）声明。 */
        public Builder withX5c(List<String> x5c)
        {
            getJwsHeader().setX5c(x5c);
            return this;
        }

        /** 向 x5c 证书链追加一条 Base64 编码证书。 */
        public Builder withX5c(String x5c)
        {
            getJwsHeader().addX5c(x5c);
            return this;
        }

        /** 向 x5c 证书链追加一张证书。 */
        public Builder withX5c(Certificate x5c)
        {
            getJwsHeader().addX5c(x5c);
            return this;
        }

        /** 设置用于签名的上下文。 */
        public Builder withSignerContext(SignatureSignerContext signatureSignerContext) {
            this.signerContext = signatureSignerContext;
            return this;
        }

        public KeyBindingJWT build() {
            return new KeyBindingJWT(jwsHeader, payload, signerContext);
        }
    }
}
