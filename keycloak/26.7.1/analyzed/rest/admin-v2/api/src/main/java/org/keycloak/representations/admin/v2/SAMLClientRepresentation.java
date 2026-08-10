/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.admin.v2;

import java.util.Objects;

import jakarta.validation.constraints.Size;

import org.keycloak.representations.admin.v2.validation.ValidCanonicalizationMethod;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonValue;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * SAML 客户端的 Admin v2 表示。
 * <p>
 * 协议固定为 {@link #PROTOCOL}（{@code saml}），涵盖 NameID 格式、签名/断言选项、
 * 绑定方式及 XML 签名算法等 SAML 特有配置。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
@Schema(description = "SAML Client configuration")
public class SAMLClientRepresentation extends BaseClientRepresentation {
    /** SAML 协议鉴别值。 */
    public static final String PROTOCOL = "saml";

    /** 默认将协议设为 {@link #PROTOCOL}。 */
    public SAMLClientRepresentation() {
        this.protocol = PROTOCOL;
    }

    /** SAML NameID 格式枚举（JSON 为小写字符串）。 */
    public enum NameIdFormat {
        /** 用户名作为 NameID。 */
        USERNAME, EMAIL, PERSISTENT, TRANSIENT;

        @JsonValue
        public String toJson() {
            return name().toLowerCase();
        }

        @JsonCreator
        public static NameIdFormat fromJson(String value) {
            return value == null ? null : valueOf(value.toUpperCase());
        }
    }

    /** XML 数字签名算法枚举。 */
    public enum SignatureAlgorithm {
        RSA_SHA1, RSA_SHA256, RSA_SHA256_MGF1, RSA_SHA512, RSA_SHA512_MGF1, DSA_SHA1;

        @JsonCreator
        public static SignatureAlgorithm fromJson(String value) {
            return value == null ? null : valueOf(value);
        }
    }

    /** 主体使用的 NameID 格式。 */
    @JsonPropertyDescription("Name ID format to use for the subject")
    private NameIdFormat nameIdFormat;

    /** 是否强制使用指定的 NameID 格式（忽略客户端请求）。 */
    @JsonPropertyDescription("Force the specified Name ID format even if the client requests a different one")
    private Boolean forceNameIdFormat;

    /** 是否在 SAML 响应中包含 AuthnStatement。 */
    @JsonPropertyDescription("Include AuthnStatement in the SAML response")
    private Boolean includeAuthnStatement;

    /** 是否在服务端对 SAML 文档签名。 */
    @JsonPropertyDescription("Sign SAML documents on the server side")
    private Boolean signDocuments;

    /** 是否对 SAML 断言签名。 */
    @JsonPropertyDescription("Sign SAML assertions")
    private Boolean signAssertions;

    /** 是否要求客户端对 SAML 请求签名。 */
    @JsonPropertyDescription("Require client to sign SAML requests")
    private Boolean clientSignatureRequired;

    /** 是否强制 SAML 响应使用 POST 绑定。 */
    @JsonPropertyDescription("Force POST binding for SAML responses")
    private Boolean forcePostBinding;

    /** 是否使用前通道注销（浏览器重定向）。 */
    @JsonPropertyDescription("Use front-channel logout (browser redirect)")
    private Boolean frontChannelLogout;

    /** 签名 SAML 文档时使用的签名算法。 */
    @JsonPropertyDescription("Signature algorithm for signing SAML documents")
    private SignatureAlgorithm signatureAlgorithm;

    /** XML 签名的规范化方法 URI。 */
    @ValidCanonicalizationMethod
    @JsonPropertyDescription("Canonicalization method for XML signatures")
    private String signatureCanonicalizationMethod;

    /** 用于签名的 X.509 证书（PEM 内容，不含头尾）。 */
    @Size(max = 65536)
    @JsonPropertyDescription("X.509 certificate for signing (PEM format, without headers)")
    private String signingCertificate;

    /** 是否允许 ECP（Enhanced Client or Proxy）流。 */
    @JsonPropertyDescription("Allow ECP (Enhanced Client or Proxy) flow")
    private Boolean allowEcpFlow;

    public NameIdFormat getNameIdFormat() {
        return nameIdFormat;
    }

    public void setNameIdFormat(NameIdFormat nameIdFormat) {
        this.nameIdFormat = nameIdFormat;
    }

    public Boolean getForceNameIdFormat() {
        return forceNameIdFormat;
    }

    public void setForceNameIdFormat(Boolean forceNameIdFormat) {
        this.forceNameIdFormat = forceNameIdFormat;
    }

    public Boolean getIncludeAuthnStatement() {
        return includeAuthnStatement;
    }

    public void setIncludeAuthnStatement(Boolean includeAuthnStatement) {
        this.includeAuthnStatement = includeAuthnStatement;
    }

    public Boolean getSignDocuments() {
        return signDocuments;
    }

    public void setSignDocuments(Boolean signDocuments) {
        this.signDocuments = signDocuments;
    }

    public Boolean getSignAssertions() {
        return signAssertions;
    }

    public void setSignAssertions(Boolean signAssertions) {
        this.signAssertions = signAssertions;
    }

    public Boolean getClientSignatureRequired() {
        return clientSignatureRequired;
    }

    public void setClientSignatureRequired(Boolean clientSignatureRequired) {
        this.clientSignatureRequired = clientSignatureRequired;
    }

    public Boolean getForcePostBinding() {
        return forcePostBinding;
    }

    public void setForcePostBinding(Boolean forcePostBinding) {
        this.forcePostBinding = forcePostBinding;
    }

    public Boolean getFrontChannelLogout() {
        return frontChannelLogout;
    }

    public void setFrontChannelLogout(Boolean frontChannelLogout) {
        this.frontChannelLogout = frontChannelLogout;
    }

    public SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getSignatureCanonicalizationMethod() {
        return signatureCanonicalizationMethod;
    }

    public void setSignatureCanonicalizationMethod(String signatureCanonicalizationMethod) {
        this.signatureCanonicalizationMethod = signatureCanonicalizationMethod;
    }

    public String getSigningCertificate() {
        return signingCertificate;
    }

    public void setSigningCertificate(String signingCertificate) {
        this.signingCertificate = signingCertificate;
    }

    public Boolean getAllowEcpFlow() {
        return allowEcpFlow;
    }

    public void setAllowEcpFlow(Boolean allowEcpFlow) {
        this.allowEcpFlow = allowEcpFlow;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SAMLClientRepresentation)) return false;
        if (!super.equals(o)) return false;
        SAMLClientRepresentation that = (SAMLClientRepresentation)o;
        return Objects.equals(nameIdFormat, that.nameIdFormat) 
                && Objects.equals(forceNameIdFormat, that.forceNameIdFormat) 
                && Objects.equals(includeAuthnStatement, that.includeAuthnStatement) 
                && Objects.equals(signDocuments, that.signDocuments) 
                && Objects.equals(signAssertions, that.signAssertions) 
                && Objects.equals(clientSignatureRequired, that.clientSignatureRequired) 
                && Objects.equals(forcePostBinding, that.forcePostBinding) 
                && Objects.equals(frontChannelLogout, that.frontChannelLogout) 
                && Objects.equals(signatureAlgorithm, that.signatureAlgorithm) 
                && Objects.equals(signatureCanonicalizationMethod, that.signatureCanonicalizationMethod) 
                && Objects.equals(signingCertificate, that.signingCertificate) 
                && Objects.equals(allowEcpFlow, that.allowEcpFlow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nameIdFormat, forceNameIdFormat, includeAuthnStatement, 
                signDocuments, signAssertions, clientSignatureRequired, forcePostBinding, 
                frontChannelLogout, signatureAlgorithm, signatureCanonicalizationMethod, 
                signingCertificate, allowEcpFlow);
    }
}
