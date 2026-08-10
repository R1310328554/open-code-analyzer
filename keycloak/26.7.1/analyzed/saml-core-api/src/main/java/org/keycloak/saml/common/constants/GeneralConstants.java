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
package org.keycloak.saml.common.constants;

import java.nio.charset.Charset;

/**
 * PicketLink / Keycloak SAML 通用配置与上下文键名常量。
 * Constants
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 8, 2009
 */
public interface GeneralConstants {
    /** 断言有效期配置键。 */
    String ASSERTIONS_VALIDITY = "ASSERTIONS_VALIDITY";
    /** 时钟偏差容忍配置键。 */
    String CLOCK_SKEW = "CLOCK_SKEW";

    /** 断言 ID 上下文键。 */
    String ASSERTION_ID = "ASSERTION_ID";

    /** 断言对象上下文键。 */
    String ASSERTION = "ASSERTION";

    /** 属性集合上下文键。 */
    String ATTRIBUTES = "ATTRIBUTES";

    /** 属性键名列表上下文键。 */
    String ATTRIBUTE_KEYS = "ATTRIBUTE_KEYS";

    /** 是否优先使用 FriendlyName 选择属性。 */
    String ATTRIBUTE_CHOOSE_FRIENDLY_NAME = "ATTRIBUTE_CHOOSE_FRIENDLY_NAME";

    /** 属性管理器配置键。 */
    String ATTIBUTE_MANAGER = "ATTRIBUTE_MANAGER";

    /** 是否启用审计日志。 */
    String AUDIT_ENABLE = "picketlink.audit.enable";

    /** 审计辅助类配置键。 */
    String AUDIT_HELPER = "AUDIT_HELPER";

    /** 审计安全域配置键。 */
    String AUDIT_SECURITY_DOMAIN = "picketlink.audit.securitydomain";

    /** 全局配置对象上下文键。 */
    String CONFIGURATION = "CONFIGURATION";

    /** PicketLink 主配置文件路径。 */
    String CONFIG_FILE_LOCATION = "/WEB-INF/picketlink.xml";

    /** 配置提供者类名配置键。 */
    String CONFIG_PROVIDER = "CONFIG_PROVIDER";

    /** Web 应用上下文路径键。 */
    String CONTEXT_PATH = "CONTEXT_PATH";

    /** 已弃用的 ID 联合配置文件路径。 */
    String DEPRECATED_CONFIG_FILE_LOCATION = "/WEB-INF/picketlink-idfed.xml";

    /** 本地登出标识。 */
    String LOCAL_LOGOUT = "LLO";

    /** 全局登出标识。 */
    String GLOBAL_LOGOUT = "GLO";

    /** 处理器配置文件路径。 */
    String HANDLER_CONFIG_FILE_LOCATION = "/WEB-INF/picketlink-handlers.xml";

    /** 身份服务器角色标识。 */
    String IDENTITY_SERVER = "IDENTITY_SERVER";

    /** 身份参与者栈上下文键。 */
    String IDENTITY_PARTICIPANT_STACK = "IDENTITY_PARTICIPANT_STACK";

    /** 是否忽略签名验证。 */
    String IGNORE_SIGNATURES = "IGNORE_SIGNATURES";

    /** 密钥对配置键。 */
    String KEYPAIR = "KEYPAIR";

    /** 登录类型配置键。 */
    String LOGIN_TYPE = "LOGIN_TYPE";

    /** 登出页面配置键。 */
    String LOGOUT_PAGE = "LOGOUT_PAGE";

    /** 默认登出 JSP 页面路径。 */
    String LOGOUT_PAGE_NAME = "/logout.jsp";

    /** NameID 格式配置键。 */
    String NAMEID_FORMAT = "NAMEID_FORMAT";

    /** 主体标识会话属性键。 */
    String PRINCIPAL_ID = "picketlink.principal";

    /** SAML RelayState 参数名。 */
    String RELAY_STATE = "RelayState";

    /** 角色集合上下文键。 */
    String ROLES = "ROLES";

    /** 角色会话属性键。 */
    String ROLES_ID = "picketlink.roles";

    /** 角色生成器配置键。 */
    String ROLE_GENERATOR = "ROLE_GENERATOR";

    /** 角色校验器配置键。 */
    String ROLE_VALIDATOR = "ROLE_VALIDATOR";

    /** 是否忽略角色校验。 */
    String ROLE_VALIDATOR_IGNORE = "ROLE_VALIDATOR_IGNORE";

    /** URL 参数/属性键。 */
    String URL = "url";

    /** HTTP 表单/查询参数：SAMLRequest。 */
    String SAML_REQUEST_KEY = "SAMLRequest";

    /** HTTP 表单/查询参数：SAMLResponse。 */
    String SAML_RESPONSE_KEY = "SAMLResponse";

    /** HTTP 参数：SAML Artifact。 */
    String SAML_ARTIFACT_KEY = "SAMLart";

    /** HTTP-Redirect 绑定签名算法参数名。 */
    String SAML_SIG_ALG_REQUEST_KEY = "SigAlg";

    /** HTTP-Redirect 绑定签名值参数名。 */
    String SAML_SIGNATURE_REQUEST_KEY = "Signature";

    /** IdP 是否强制 POST 绑定。 */
    String SAML_IDP_STRICT_POST_BINDING = "SAML_IDP_STRICT_POST_BINDING";

    // Should JAXP Factory operations cache the TCCL and revert after operation?
    /** JAXP 工厂操作是否缓存并恢复 TCCL。 */
    String TCCL_JAXP = "picketlink.jaxp.tccl";

    /** 时区配置属性名。 */
    String TIMEZONE = "picketlink.timezone";

    /** 默认时区配置键。 */
    String TIMEZONE_DEFAULT = "TIMEZONE_DEFAULT";

    /** 解密密钥配置键。 */
    String DECRYPTING_KEY = "DECRYPTING_KEY";

    /** SP SSO 元数据描述符键。 */
    String SP_SSO_METADATA_DESCRIPTOR = "SP_SSO_METADATA_DESCRIPTOR";

    /** IdP SSO 元数据描述符键。 */
    String IDP_SSO_METADATA_DESCRIPTOR = "IDP_SSO_METADATA_DESCRIPTOR";

    /** SSO 元数据描述符通用键。 */
    String SSO_METADATA_DESCRIPTOR = "SSO_METADATA_DESCRIPTOR";

    /** 发送方公钥配置键。 */
    String SENDER_PUBLIC_KEY = "SENDER_PUBLIC_KEY";

    /** 是否对出站消息签名。 */
    String SIGN_OUTGOING_MESSAGES = "SIGN_OUTGOING_MESSAGES";

    /** 是否支持数字签名。 */
    String SUPPORTS_SIGNATURES = "SUPPORTS_SIGNATURES";

    /** 会话属性映射上下文键。 */
    String SESSION_ATTRIBUTE_MAP = "SESSION_ATTRIBUTE_MAP";

    /** 登录表单用户名字段名。 */
    String USERNAME_FIELD = "JBID_USERNAME";

    /** 登录表单密码字段名。 */
    String PASS_FIELD = "JBID_PASSWORD";

    /** 认证请求 ID 上下文键。 */
    String AUTH_REQUEST_ID = "AUTH_REQUEST_ID";
    /** 默认错误页面路径。 */
    String ERROR_PAGE_NAME = "/error.jsp";
    /** SAML 加密密钥长度配置键。 */
    String SAML_ENC_KEY_SIZE = "SAML_ENC_KEY_SIZE";
    /** SAML 加密算法配置键。 */
    String SAML_ENC_ALGORITHM = "SAML_ENC_ALGORITHM";

    /**
     * {@link SAML2AuthenticationHandler} 配置项：将断言存入 {@link HttpSession} 时使用的会话属性名。
     * <p>{@link SAML2AuthenticationHandler} configuration option to set the assertion into the {@link
     * HttpSession}.</p>
     */
    String ASSERTION_SESSION_ATTRIBUTE_NAME = "ASSERTION_SESSION_ATTRIBUTE_NAME";

    /** X.509 证书配置/上下文键。 */
    String X509CERTIFICATE = "X509CERTIFICATE";

    /** 允许的认证上下文类配置键。 */
    String AUTHN_CONTEXT_CLASSES = "AUTHN_CONTEXT_CLASSES";
    /** RequestedAuthnContext 比较方式配置键。 */
    String REQUESTED_AUTHN_CONTEXT_COMPARISON = "REQUESTED_AUTHN_CONTEXT_COMPARISON";

    /** WS-Trust 密钥是否 Base64 编码。 */
    String BASE64_ENCODE_WSTRUST_SECRET_KEY = "picketlink.wstrust.base64_encode_wstrust_secret_key";

    /** Ajax 请求常用 HTTP 头 X-Requested-With。 */
    String HTTP_HEADER_X_REQUESTED_WITH = "X-Requested-With";

    /** SAML 消息字符集名称（可通过系统属性覆盖，默认 UTF-8）。 */
    public static final String  SAML_CHARSET_NAME = System.getProperty("keycloak.saml.saml_message_charset", "UTF-8");
    /** SAML 消息字符集实例。 */
    public static final Charset SAML_CHARSET = Charset.forName(SAML_CHARSET_NAME);
}
