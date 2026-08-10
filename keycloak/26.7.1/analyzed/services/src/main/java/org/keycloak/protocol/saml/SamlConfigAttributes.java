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

package org.keycloak.protocol.saml;

import org.keycloak.services.util.CertificateInfoHelper;

/**
 * SAML 客户端配置属性键名常量。
 * <p>对应 {@link ClientModel} 属性，用于签名、加密、绑定、NameID 及元数据等 SAML 设置。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface SamlConfigAttributes {
    /** 客户端签名私钥（PEM） */
    String SAML_SIGNING_PRIVATE_KEY = "saml.signing.private.key";
    /** XML 签名规范化方法 */
    String SAML_CANONICALIZATION_METHOD_ATTRIBUTE = "saml_signature_canonicalization_method";
    /** 签名算法名称 */
    String SAML_SIGNATURE_ALGORITHM = "saml.signature.algorithm";
    /** NameID 格式（短名） */
    String SAML_NAME_ID_FORMAT_ATTRIBUTE = "saml_name_id_format";
    /** 是否包含 AuthnStatement */
    String SAML_AUTHNSTATEMENT = "saml.authnstatement";
    /** 是否包含 OneTimeUse 条件 */
    String SAML_ONETIMEUSE_CONDITION = "saml.onetimeuse.condition";
    /** 是否强制 NameID 格式 */
    String SAML_FORCE_NAME_ID_FORMAT_ATTRIBUTE = "saml_force_name_id_format";
    /** 是否强制 Artifact 绑定 */
    String SAML_ARTIFACT_BINDING = "saml.artifact.binding";
    /** 服务器（IdP）是否签名 SAML 响应 */
    String SAML_SERVER_SIGNATURE = "saml.server.signature";
    /** 是否在 Extensions 中包含 KeyInfo */
    String SAML_SERVER_SIGNATURE_KEYINFO_EXT = "saml.server.signature.keyinfo.ext";
    /** KeyInfo KeyName 转换策略 */
    String SAML_SERVER_SIGNATURE_KEYINFO_KEY_NAME_TRANSFORMER = "saml.server.signature.keyinfo.xmlSigKeyInfoKeyNameTransformer";
    /** 是否强制 POST 绑定 */
    String SAML_FORCE_POST_BINDING = "saml.force.post.binding";
    /** 是否对断言单独签名 */
    String SAML_ASSERTION_SIGNATURE = "saml.assertion.signature";
    /** 是否加密断言 */
    String SAML_ENCRYPT = "saml.encrypt";
    /** 是否要求客户端签名 AuthnRequest */
    String SAML_CLIENT_SIGNATURE_ATTRIBUTE = "saml.client.signature";
    /** 客户端签名 X509 证书 */
    String SAML_SIGNING_CERTIFICATE_ATTRIBUTE = "saml.signing." + CertificateInfoHelper.X509CERTIFICATE;
    /** 客户端加密 X509 证书 */
    String SAML_ENCRYPTION_CERTIFICATE_ATTRIBUTE = "saml.encryption." + CertificateInfoHelper.X509CERTIFICATE;
    /** 客户端加密私钥 */
    String SAML_ENCRYPTION_PRIVATE_KEY_ATTRIBUTE = "saml.encryption." + CertificateInfoHelper.PRIVATE_KEY;
    /** 内容加密算法 */
    String SAML_ENCRYPTION_ALGORITHM = "saml.encryption.algorithm";
    /** 密钥传输加密算法 */
    String SAML_ENCRYPTION_KEY_ALGORITHM = "saml.encryption.keyAlgorithm";
    /** 加密摘要方法 */
    String SAML_ENCRYPTION_DIGEST_METHOD = "saml.encryption.digestMethod";
    /** OAEP 掩码生成函数 */
    String SAML_ENCRYPTION_MASK_GENERATION_FUNTION = "saml.encryption.maskGenerationFunction";
    /** 断言有效期（秒） */
    String SAML_ASSERTION_LIFESPAN = "saml.assertion.lifespan";
    /** Artifact Binding SourceID 标识 */
    String SAML_ARTIFACT_BINDING_IDENTIFIER = "saml.artifact.binding.identifier";
    /** 是否允许 ECP 流程 */
    String SAML_ALLOW_ECP_FLOW = "saml.allow.ecp.flow";
    /** 是否使用元数据描述符 URL */
    String SAML_USE_METADATA_DESCRIPTOR_URL = "saml.useMetadataDescriptorUrl";
    /** SP 元数据描述符 URL */
    String SAML_METADATA_DESCRIPTOR_URL = "saml.metadataDescriptorUrl";
    /** 元数据描述符缓存秒数 */
    String SAML_DESCRIPTOR_CACHE_SECONDS = "saml.descriptor.cache.seconds";
}
