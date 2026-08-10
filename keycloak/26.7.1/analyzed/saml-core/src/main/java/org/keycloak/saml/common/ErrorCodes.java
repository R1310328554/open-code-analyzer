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
package org.keycloak.saml.common;

/**
 * PicketLink 错误码常量接口。
 * <p>各常量值为带 PL 前缀的错误消息前缀，用于异常与日志输出。</p>
 * <p>完整列表参见 <a href="https://docs.jboss.org/author/display/PLINK/PicketLink+Error+Codes">PicketLink Error Codes</a>。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 4, 2011
 */
public interface ErrorCodes {

    /** 断言续期异常。 */
    String ASSERTION_RENEWAL_EXCEPTION = "PL00103:Assertion Renewal Exception:";

    /** 审计管理器未设置。 */
    String AUDIT_MANAGER_NULL = "PL00028: Audit Manager Is Not Set";

    /** AuthnRequest ID 校验失败。 */
    String AUTHN_REQUEST_ID_VERIFICATION_FAILED = "PL00104:Authn Request ID verification failed:";

    /** 类未能加载。 */
    String CLASS_NOT_LOADED = "PL00085: Class Not Loaded:";

    /** 无法创建实例。 */
    String CANNOT_CREATE_INSTANCE = "PL00086: Cannot create instance of:";

    /** DOM 缺少 Document 根元素。 */
    String DOM_MISSING_DOC_ELEMENT = "PL00098: Missing Document Element:";

    /** DOM 缺少指定元素。 */
    String DOM_MISSING_ELEMENT = "PL00099: Missing Element:";

    /** 断言已过期。 */
    String EXPIRED_ASSERTION = "PL00079: Assertion has expired:";

    /** 解析器：期望 xsi:type。 */
    String EXPECTED_XSI = "PL00072: Parser: Expected xsi:type";

    /** 解析器：期望起始标签。 */
    String EXPECTED_TAG = "PL00066: Parser : Expected start tag:";

    /** 解析器：期望命名空间。 */
    String EXPECTED_NAMESPACE = "PL00107: Parser : Expected start element namespace:";

    /** 解析器：期望文本值。 */
    String EXPECTED_TEXT_VALUE = "PL00071: Parser: Expected text value:";

    /** 解析器：期望结束标签。 */
    String EXPECTED_END_TAG = "PL00066: Parser : Expected end tag:";

    /** 解析失败。 */
    String FAILED_PARSING = "PL00067: Parsing has failed:";

    /** 文件未找到。 */
    String FILE_NOT_LOCATED = "PL00075: File could not be located :";

    /** IdP 认证失败。 */
    String IDP_AUTH_FAILED = "PL00015: IDP Authentication Failed:";

    /** IdP Web 浏览器阀配置文件缺失。 */
    String IDP_WEBBROWSER_VALVE_CONF_FILE_MISSING = "PL00017: Configuration File missing:";

    /** 无效断言。 */
    String INVALID_ASSERTION = "PL00080: Invalid Assertion:";

    /** 无效数字签名。 */
    String INVALID_DIGITAL_SIGNATURE = "PL00009: Invalid Digital Signature:";

    /** 注入值缺失。 */
    String INJECTED_VALUE_MISSING = "PL00077: Injected Value Missing:";

    /** IssuerInfo 缺少状态码。 */
    String ISSUER_INFO_MISSING_STATUS_CODE = "PL00085: IssuerInfo missing status code :";

    /** KeyStoreKeyManager：域别名缺失。 */
    String KEYSTOREKEYMGR_DOMAIN_ALIAS_MISSING = "PL00058: KeyStoreKeyManager : Domain Alias missing for :";

    /** KeyStoreKeyManager：别名为空。 */
    String KEYSTOREKEYMGR_NULL_ALIAS = "PL00059: KeyStoreKeyManager : Alias is null";

    /** KeyStoreKeyManager：KeyStore 为空。 */
    String KEYSTOREKEYMGR_NULL_KEYSTORE = "PL00055: KeyStoreKeyManager : KeyStore is null";

    /** KeyStoreKeyManager：签名密钥密码为空。 */
    String KEYSTOREKEYMGR_NULL_SIGNING_KEYPASS = "PL00057: KeyStoreKeyManager :: Signing Key Pass is null";

    /** KeyStoreKeyManager：加密密钥密码为空。 */
    String KEYSTOREKEYMGR_NULL_ENCRYPTION_KEYPASS = "PL00189: KeyStoreKeyManager :: Encryption Key Pass is null";

    /** KeyStoreKeyManager：KeyStore 文件未找到。 */
    String KEYSTOREKEYMGR_KEYSTORE_NOT_LOCATED = "PL00056: KeyStoreKeyManager: Keystore not located:";

    /** 值不相等。 */
    String NOT_EQUAL = "PL00094: Not equal:";

    /** 功能尚未实现。 */
    String NOT_IMPLEMENTED_YET = "PL00082: Not Implemented Yet: ";

    /** 对象不可序列化。 */
    String NOT_SERIALIZABLE = "PL00093: Not Serializable:";

    /** 空参数。 */
    String NULL_ARGUMENT = "PL00078: Null Parameter:";

    /** IssueInstant 为空。 */
    String NULL_ISSUE_INSTANT = "PL00088: Null IssueInstant";

    /** 解析器：起始元素为空。 */
    String NULL_START_ELEMENT = "PL00068: Parser : Start Element is null";

    /** 空值。 */
    String NULL_VALUE = "PL00092: Null Value:";

    /** 配置选项未设置。 */
    String OPTION_NOT_SET = "PL00076: Option not set:";

    /** 解析错误。 */
    String PARSING_ERROR = "PL00074: Parsing Error:";

    /** 主体（Principal）未找到。 */
    String PRINCIPAL_NOT_FOUND = "PL00022: Principal Not Found";

    /** 通用处理异常。 */
    String PROCESSING_EXCEPTION = "PL00102: Processing Exception:";

    /** 解析器：缺少必需属性。 */
    String REQD_ATTRIBUTE = "PL00063: Parser: Required attribute missing: ";

    /** 资源未找到。 */
    String RESOURCE_NOT_FOUND = "PL00018: Resource not found:";

    /** SAML2STSLoginModule：STS 配置文件未指定。 */
    String SAML2STSLM_CONF_FILE_MISSING = "PL00039: SAML2STSLoginModule: Failed to validate assertion: STS configuration file not specified";

    /** SP 响应非 Catalina 类型。 */
    String SERVICE_PROVIDER_NOT_CATALINA_RESPONSE = "PL00026: Response was not of type catalina response";

    /** SP 服务端异常。 */
    String SERVICE_PROVIDER_SERVER_EXCEPTION = "PL00032: Service Provider :: Server Exception";

    /** 两个值不应相同。 */
    String SHOULD_NOT_BE_THE_SAME = "PL00016: Should not be the same:";

    /** 签名处理失败。 */
    String SIGNING_PROCESS_FAILURE = "PL00100: Signing Process Failure:";

    /** STS：无法定位客户端公钥。 */
    String STS_CLIENT_PUBLIC_KEY_ERROR = "PL00008: Unable to locate client public key";

    /** STS：配置文件解析错误。 */
    String STS_CONFIGURATION_FILE_PARSING_ERROR = "PL00005: Error parsing the configuration file:";

    /** STS：配置异常。 */
    String STS_CONFIGURATION_EXCEPTION = "PL00002: Encountered configuration exception:";

    /** STS：组合密钥生成错误。 */
    String STS_COMBINED_SECRET_KEY_ERROR = "PL00006: Error generating combined secret key:";

    /** STS：处理令牌请求时异常。 */
    String STS_EXCEPTION_HANDLING_TOKEN_REQ = "PL00003: Exception in handling token request: ";

    /** STS：未找到安全令牌提供者。 */
    String STS_NO_TOKEN_PROVIDER = "PL00013: No Security Token Provider found in configuration:[";

    /** STS：无效安全令牌请求。 */
    String STS_INVALID_TOKEN_REQUEST = "PL00001: Invalid security token request";

    /** STS：无效请求类型。 */
    String STS_INVALID_REQUEST_TYPE = "PL00001: Invalid request type: ";

    /** STS：获取服务公钥失败。 */
    String STS_PUBLIC_KEY_ERROR = "PL00010: Error obtaining public key for service: ";

    /** STS：获取公钥证书失败。 */
    String STS_PUBLIC_KEY_CERT = "PL00012: Error obtaining public key certificate:";

    /** STS：写入响应失败。 */
    String STS_RESPONSE_WRITING_ERROR = "PL00004: Error writing response: ";

    /** STS：获取签名密钥对失败。 */
    String STS_SIGNING_KEYPAIR_ERROR = "PL00011: Error obtaining signing key pair:";

    /** STS：无法构造密钥管理器。 */
    String STS_UNABLE_TO_CONSTRUCT_KEYMGR = "PL00007: Unable to construct the key manager:";

    /** 系统属性缺失。 */
    String SYSTEM_PROPERTY_MISSING = "PL00087: System Property missing:";

    /** 信任密钥管理器缺失。 */
    String TRUST_MANAGER_MISSING = "PL000023: Trust Key Manager Missing";

    /** 解析器：无法解析空令牌请求。 */
    String UNABLE_PARSING_NULL_TOKEN = "PL00073: Parser: Unable to parse token request: security token is null";

    /** 无法回退到本地认证。 */
    String UNABLE_LOCAL_AUTH = "PL00035: Unable to fallback on local auth:";

    /** 解析器：未知结束元素。 */
    String UNKNOWN_END_ELEMENT = "PL00061: Parser: Unknown End Element:";

    /** 未知对象类型。 */
    String UNKNOWN_OBJECT_TYPE = "PL00089: Unknown Object Type:";

    /** 解析器：未知起始元素。 */
    String UNKNOWN_START_ELEMENT = "PL00064: Parser: Unknown Start Element: ";

    /** 未知签名算法。 */
    String UNKNOWN_SIG_ALGO = "PL00090: Unknown Signature Algorithm:";

    /** 未知加密算法。 */
    String UNKNOWN_ENC_ALGO = "PL00097: Unknown Encryption Algorithm:";

    /** 解析器：未知标签。 */
    String UNKNOWN_TAG = "PL00062: Parser : Unknown tag:";

    /** 解析器：未知 xsi:type。 */
    String UNKNOWN_XSI = "PL0065: Parser : Unknown xsi:type=";

    /** 解析器：不支持的类型。 */
    String UNSUPPORTED_TYPE = "PL00069: Parser: Type not supported:";

    /** 校验检查失败。 */
    String VALIDATION_CHECK_FAILED = "PL00019: Validation check failed";

    /** 写入器：KeyInfo 内容为空。 */
    String WRITER_INVALID_KEYINFO_NULL_CONTENT = "PL00091: Writer: Invalid KeyInfo object: content cannot be empty";

    /** 写入器：空值。 */
    String WRITER_NULL_VALUE = "PL00083: Writer: Null Value:";

    /** 写入器：应为 StartElement。 */
    String WRITER_SHOULD_START_ELEMENT = "PL00096: Writer: Should have been a StartElement";

    /** 写入器：未知类型。 */
    String WRITER_UNKNOWN_TYPE = "PL00081: Writer: Unknown Type:";

    /** 写入器：不支持的属性值。 */
    String WRITER_UNSUPPORTED_ATTRIB_VALUE = "PL00084: Writer: Unsupported Attribute Value:";

    /** 错误类型。 */
    String WRONG_TYPE = "PL00095: Wrong type:";

}