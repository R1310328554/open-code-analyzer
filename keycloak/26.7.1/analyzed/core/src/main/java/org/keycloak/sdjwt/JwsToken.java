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

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.OID4VCConstants;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JWS 令牌的抽象基类，适用于签发者 JWT 与持有者密钥绑定 JWT。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 *
 */
public abstract class JwsToken {

    /** JWS 头部。 */
    protected JWSHeader jwsHeader;

    /** JWT 载荷（JSON 对象节点）。 */
    protected ObjectNode payload;

    /** 序列化后的 JWS 紧凑字符串。 */
    protected String jws;

    /** 解析后的 JWS 输入对象。 */
    protected JWSInput jwsInput;

    protected JwsToken(String jws) {
        parse(jws);
    }

    protected JwsToken(JWSHeader jwsHeader, ObjectNode payload) {
        this.jwsHeader = jwsHeader;
        this.payload = payload;
    }

    protected JwsToken(JWSHeader jwsHeader, ObjectNode payload, SignatureSignerContext signerContext) {
        this.jwsHeader = jwsHeader;
        this.payload = payload;
        this.jws = sign(signerContext);
    }

    /**
     * 使用给定签名上下文对当前载荷进行签名。
     *
     * @param signerContext Keycloak 签名上下文
     * @return 签名后的 JWS 紧凑字符串
     */
    public String sign(SignatureSignerContext signerContext) {
        jws = new JWSBuilder().header(jwsHeader).jsonContent(payload).sign(signerContext);
        try {
            jwsInput = new JWSInput(jws);
            jwsHeader = jwsInput.getHeader();
            payload = jwsInput.readJsonContent(ObjectNode.class);
        } catch (JWSInputException e) {
            throw new IllegalStateException(String.format("Got invalid JWS '%s'", jws), e);
        }
        return jws;
    }

    /**
     * 验证 JWS 签名。
     *
     * @param verifier 签名验证上下文
     * @throws VerificationException 签名无效或验证过程出错
     */
    public void verifySignature(SignatureVerifierContext verifier) throws VerificationException {
        Objects.requireNonNull(verifier, "verifier must not be null");
        try {
            if (!verifier.verify(jwsInput.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8),
                                 jwsInput.getSignature())) {
                throw new VerificationException("Invalid jws signature");
            }
        } catch (Exception e) {
            throw new VerificationException(e);
        }
    }

    /** @return 载荷中声明的 SD 哈希算法，若未设置则为空 */
    public Optional<String> getSdHashAlgorithm() {
        return Optional.ofNullable(payload.get(OID4VCConstants.CLAIM_NAME_SD_HASH_ALGORITHM))
                       .map(JsonNode::textValue);
    }

    /** @return JWS 紧凑字符串 */
    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }

    /** @return 解析后的 JWS 输入 */
    public JWSInput getJwsInput() {
        return jwsInput;
    }

    public void setJwsInput(JWSInput jwsInput) {
        this.jwsInput = jwsInput;
        if (jwsInput != null) {
            setJwsHeader(jwsInput.getHeader());
            try {
                setPayload(jwsInput.readJsonContent(ObjectNode.class));
            } catch (JWSInputException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** @return JWS 头部对象 */
    public JWSHeader getJwsHeader() {
        return jwsHeader;
    }

    /** @return JWS 头部的 JSON 对象节点表示 */
    public ObjectNode getJwsHeaderAsNode() {
        return JsonSerialization.mapper.convertValue(jwsHeader, ObjectNode.class);
    }

    public void setJwsHeader(JWSHeader jwsHeader) {
        this.jwsHeader = jwsHeader;
    }

    /** @return JWT 载荷 */
    public ObjectNode getPayload() {
        return payload;
    }

    public void setPayload(ObjectNode payload) {
        this.payload = payload;
    }

    /** 从 JWS 紧凑字符串解析头部与载荷。 */
    private void parse(String jwsString) {
        try {
            this.jws = jwsString;
            this.jwsInput = new JWSInput(Objects.requireNonNull(jwsString, "jwsString must not be null"));
            this.jwsHeader = jwsInput.getHeader();
            this.payload = JsonSerialization.mapper.readValue(jwsInput.getContent(), ObjectNode.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
