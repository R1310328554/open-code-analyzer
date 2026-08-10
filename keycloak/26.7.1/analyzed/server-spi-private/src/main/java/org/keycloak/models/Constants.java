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

package org.keycloak.models;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import org.keycloak.OAuth2Constants;
import org.keycloak.crypto.Algorithm;
import org.keycloak.models.utils.SystemClientUtil;

/**
 * Keycloak 核心常量：内置客户端 ID、默认超时、认证会话 note 键、WebAuthn 策略默认值等。
 * <p>供 server-spi、services 与 admin 模块共享引用。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public final class Constants {
    /** 管理控制台内置客户端 ID。 */
    public static final String ADMIN_CONSOLE_CLIENT_ID = "security-admin-console";
    /** Admin CLI 内置客户端 ID。 */
    public static final String ADMIN_CLI_CLIENT_ID = "admin-cli";

    public static final String ACCOUNT_MANAGEMENT_CLIENT_ID = "account";
    public static final String ACCOUNT_CONSOLE_CLIENT_ID = "account-console";
    public static final String BROKER_SERVICE_CLIENT_ID = "broker";
    public static final String REALM_MANAGEMENT_CLIENT_ID = "realm-management";
    public static final String ADMIN_PERMISSIONS_CLIENT_ID = "admin-permissions";

    public static final String AUTH_BASE_URL_PROP = "${authBaseUrl}";
    public static final String AUTH_ADMIN_URL_PROP = "${authAdminUrl}";

    /** 系统预置客户端 ID 集合（不可删除）。 */
    public static final Collection<String> defaultClients = Arrays.asList(ACCOUNT_MANAGEMENT_CLIENT_ID, ACCOUNT_CONSOLE_CLIENT_ID, ADMIN_CLI_CLIENT_ID, BROKER_SERVICE_CLIENT_ID, REALM_MANAGEMENT_CLIENT_ID, ADMIN_CONSOLE_CLIENT_ID, ADMIN_PERMISSIONS_CLIENT_ID, SystemClientUtil.SYSTEM_CLIENT_ID);

    public static final String INSTALLED_APP_URN = "urn:ietf:wg:oauth:2.0:oob";

    public static final String READ_TOKEN_ROLE = "read-token";
    public static final String[] BROKER_SERVICE_ROLES = {READ_TOKEN_ROLE};
    public static final String OFFLINE_ACCESS_ROLE = OAuth2Constants.OFFLINE_ACCESS;
    public static final String DEFAULT_ROLES_ROLE_PREFIX = "default-roles";

    public static final String AUTHZ_UMA_PROTECTION = "uma_protection";
    public static final String AUTHZ_UMA_AUTHORIZATION = "uma_authorization";
    public static final String[] AUTHZ_DEFAULT_AUTHORIZATION_ROLES = {AUTHZ_UMA_AUTHORIZATION};

    /** 默认访问令牌有效期：5 分钟。 */
    public static final int DEFAULT_ACCESS_TOKEN_LIFESPAN = 300;

    /** 隐式流程访问令牌默认有效期：15 分钟。 */
    public static final int DEFAULT_ACCESS_TOKEN_LIFESPAN_FOR_IMPLICIT_FLOW_TIMEOUT = 900;
    /** 离线会话空闲超时默认：30 天。 */
    public static final int DEFAULT_OFFLINE_SESSION_IDLE_TIMEOUT = 2592000;
    /** 离线会话最大生命周期默认：60 天（KEYCLOAK-7688）。 */
    public static final int DEFAULT_OFFLINE_SESSION_MAX_LIFESPAN = 5184000;
    public static final String DEFAULT_SIGNATURE_ALGORITHM = Algorithm.RS256;
    public static final String INTERNAL_SIGNATURE_ALGORITHM = Algorithm.HS512;

    /** 用户会话空闲超时默认：30 分钟。 */
    public static final int DEFAULT_SESSION_IDLE_TIMEOUT = 1800;
    /** 用户会话最大生命周期默认：10 小时。 */
    public static final int DEFAULT_SESSION_MAX_LIFESPAN = 36000;

    /** 管理控制台「登录超时」默认：30 分钟。 */
    public static final int DEFAULT_ACCESS_CODE_LIFESPAN_LOGIN = 1800;

    /** 管理控制台「客户端登录超时」默认：1 分钟。 */
    public static final int DEFAULT_ACCESS_CODE_LIFESPAN = 60;

    /** 「登录操作超时」与「用户发起操作生命周期」默认：5 分钟。 */
    public static final int DEFAULT_ACCESS_CODE_LIFESPAN_USER_ACTION = 300;

    public static final int DEFAULT_ACTION_TOKEN_GENERATED_BY_ADMIN_LIFESPAN = 12 * 60 * 60;

    public static final String DEFAULT_WEBAUTHN_POLICY_SIGNATURE_ALGORITHMS = Algorithm.ES256+","+Algorithm.RS256;
    public static final String DEFAULT_WEBAUTHN_POLICY_RP_ENTITY_NAME = "keycloak";
    // it stands for optional parameter not specified in WebAuthn
    public static final String DEFAULT_WEBAUTHN_POLICY_NOT_SPECIFIED = "not specified";
    public static final String WEBAUTHN_POLICY_OPTION_REQUIRED = "required";
    public static final String WEBAUTHN_POLICY_OPTION_PREFERED = "preferred";
    public static final String WEBAUTHN_POLICY_OPTION_DISCOURAGED = "discouraged";
    public static final String WEBAUTHN_POLICY_OPTION_YES = "Yes";
    public static final String WEBAUTHN_POLICY_OPTION_NO = "No";

    /** WebAuthn 无密码策略在 realm 属性等处的键前缀。 */
    public static final String WEBAUTHN_PASSWORDLESS_PREFIX = "Passwordless";

    public static final String VERIFY_EMAIL_KEY = "VERIFY_EMAIL_KEY";
    public static final String EXECUTION = "execution";
    public static final String CLIENT_ID = "client_id";
    public static final String TOKEN = "token";
    public static final String TAB_ID = "tab_id";
    public static final String CLIENT_DATA = "client_data";
    public static final String REUSE_ID = "reuse_id";
    public static final String SKIP_LOGOUT = "skip_logout";
    public static final String KEY = "key";

    /** 认证流程中当前执行的 Keycloak 操作（Required Action / AIA）标识。 */
    public static final String KC_ACTION = "kc_action";

    public static final String KC_ACTION_PARAMETER = "kc_action_parameter";
    // parameter used by some actions to skip executing it if a credential for that type already exists for the user
    public static final String KC_ACTION_PARAMETER_SKIP_IF_EXISTS = "skip_if_exists";
    public static final String KC_ACTION_STATUS = "kc_action_status";
    public static final String KC_ACTION_EXECUTING = "kc_action_executing";
    /**
     * 认证会话属性：AIA（Application-Initiated Action）是否被强制执行（不可取消）。
     * <p>示例：AIA 对应的操作同时为用户必需操作（如 UPDATE_PASSWORD）。</p>
     */
    public static final String KC_ACTION_ENFORCED = "kc_action_enforced";
    public static final int KC_ACTION_MAX_AGE = 300;
    public static final String MAX_AUTH_AGE_KEY = "max_auth_age";


    public static final String IS_AIA_REQUEST = "IS_AIA_REQUEST";
    public static final String AIA_SILENT_CANCEL = "silent_cancel";
    public static final String AUTHENTICATION_EXECUTION = "authenticationExecution";
    public static final String CREDENTIAL_ID = "credentialId";

    public static final String SKIP_LINK = "skipLink";
    public static final String TEMPLATE_ATTR_ACTION_URI = "actionUri";
    public static final String TEMPLATE_ATTR_REQUIRED_ACTIONS = "requiredActions";
    public static final String IGNORE_ACCEPT_LANGUAGE_HEADER = "IGNORE_ACCEPT_LANGUAGE_HEADER";

    // 各类上下文数据映射中用户属性的键前缀
    public static final String USER_ATTRIBUTES_PREFIX = "user.attributes.";

    // 更新代理用户时映射器已授予的角色
    public static final String MAPPER_GRANTED_ROLES = "MAPPER_GRANTED_ROLES";

    // 更新代理用户时映射器已分配的组
    public static final String MAPPER_GRANTED_GROUPS = "MAPPER_GRANTED_GROUPS";

    public static final String MAPPER_SESSION_NOTES = "MAPPER_SESSION_NOTES";

    // 指示 admin REST 端点重新生成 realm 密钥
    public static final String GENERATE = "GENERATE";

    public static final int DEFAULT_MAX_RESULTS = 100;
    /** {@code DefaultValue} 注解使用的字符串形式默认分页上限（{@link #DEFAULT_MAX_RESULTS}）。 */
    public static final String DEFAULT_MAX_RESULTS_STR = "" + DEFAULT_MAX_RESULTS;

    // 认证器（等组件）配置中多值合并为单字符串时的分隔符
    public static final String CFG_DELIMITER = "##";

    public static final String INCLUDE_REDIRECTS = "+";

    // 解析 {@link #CFG_DELIMITER} 的正则，性能优于 String.split
    public static final Pattern CFG_DELIMITER_PATTERN = Pattern.compile("\\s*" + CFG_DELIMITER + "\\s*");

    public static final String OFFLINE_ACCESS_SCOPE_CONSENT_TEXT = "${offlineAccessScopeConsentText}";

    /**
     * 设为 {@link KeycloakSession} 属性时，指示存储层批量写入。
     */
    public static final String STORAGE_BATCH_ENABLED = "org.keycloak.storage.batch_enabled";

    /** 在 {@code #STORAGE_BATCH_ENABLED} 启用时指定批大小。 */
    public static final String STORAGE_BATCH_SIZE = "org.keycloak.storage.batch_size";

    public static final String SNAPSHOT_VERSION = "999.0.0-SNAPSHOT";

    // 客户端策略（Client Policies）realm 属性键
    public static final String CLIENT_PROFILES = "client-policies.profiles";
    public static final String CLIENT_POLICIES = "client-policies.policies";


    // 认证会话 note：当前进行中的认证 LoA（Level of Assurance）
    public static final String LEVEL_OF_AUTHENTICATION = "level-of-authentication";

    // 认证执行配置键：存储配置的认证参考值
    public static final String AUTHENTICATION_EXECUTION_REFERENCE_VALUE = "default.reference.value";
    public static final String AUTHENTICATION_EXECUTION_REFERENCE_MAX_AGE = "default.reference.maxAge";

    // 认证会话 note：已完成认证执行及其时间的序列化映射
    public static final String AUTHENTICATORS_COMPLETED = "authenticators-completed";

    // 认证/用户会话 note：各 LoA 及认证时间，用于判断何时需重新认证
    public static final String LOA_MAP = "loa-map";

    public static final String REQUESTED_LEVEL_OF_AUTHENTICATION = "requested-level-of-authentication";
    public static final String FORCE_LEVEL_OF_AUTHENTICATION = "force-level-of-authentication";
    public static final String ACR_LOA_MAP = "acr.loa.map";
    public static final String ACR_URI_MAP = "acr.uri.map";
    public static final String DEFAULT_ACR_VALUES = "default.acr.values";
    public static final String MINIMUM_ACR_VALUE = "minimum.acr.value";
    public static final int MINIMUM_LOA = 0;
    public static final int NO_LOA = -1;

    public static final String SESSION_NOTE_LIGHTWEIGHT_USER = "keycloak.userModel";

    public static final String USE_LIGHTWEIGHT_ACCESS_TOKEN_ENABLED = "client.use.lightweight.access.token.enabled";

    public static final String SUPPORT_JWT_CLAIM_IN_INTROSPECTION_RESPONSE_ENABLED = "client.introspection.response.allow.jwt.claim.enabled";

    public static final String TOTP_SECRET_KEY = "TOTP_SECRET_KEY";

    // 认证会话过期但浏览器已登录时返回客户端的错误消息键
    public static final String AUTHENTICATION_EXPIRED_MESSAGE = "authentication_expired";

    // attribute name used in apps to mark that it is an admin console and its azp is allowed
    public static final String SECURITY_ADMIN_CONSOLE_ATTR = "security.admin.console";

    // 标记客户端为 realm 内置客户端的属性名
    public static final String REALM_CLIENT = "realm_client";

    // 认证会话 note：请求的认证流程
    public static final String REQUESTED_AUTHENTICATION_FLOW = "requested-authentication-flow";

    public static final String AUTHENTICATION_FLOW_LEVEL_OF_AUTHENTICATION = "authentication-flow-level-of-authentication";

    // 认证会话 note：客户端策略条件匹配的 ACR 值
    public static final String CLIENT_POLICY_REQUESTED_ACR = "client-policy-requested-acr";

    // 标准令牌交换中请求受众对应客户端的属性名
    public static final String REQUESTED_AUDIENCE_CLIENTS = "req-aud-clients";
    // 刷新令牌中记录请求受众的 claim
    public static final String REQUESTED_AUDIENCE = "req-aud";
    // 客户端会话上下文 note：令牌授予类型
    public static final String GRANT_TYPE = OAuth2Constants.GRANT_TYPE;
    // 客户端会话 note：令牌交换主体客户端
    public static final String TOKEN_EXCHANGE_SUBJECT_CLIENT = "token_exchange_subject_client";

    // 临时用户会话 note：由持久会话派生（值可为 online/offline）
    public static final String CREATED_FROM_PERSISTENT = "created_from_persistent";
    public static final String CREATED_FROM_PERSISTENT_ONLINE = "online";
    public static final String CREATED_FROM_PERSISTENT_OFFLINE = "offline";

    // OpenID Connect 协议提供者 ID
    public static final String OIDC_PROTOCOL = "openid-connect";

    // 客户端会话上下文内部 note：授权详情响应
    public static final String AUTHORIZATION_DETAILS_RESPONSE = "authorization_details_response";

    // 客户端会话上下文内部 note：授权请求 URI
    public static final String AUTHORIZATION_REQUEST_URI = "authorization_request_uri";

    // realm 导入属性：除导入定义中的作用域外是否创建默认客户端作用域
    // 省略或为 false 且导入已定义至少一个作用域时，不创建默认作用域
    public static final String CREATE_DEFAULT_CLIENT_SCOPES = "CreateDefaultClientScopes";
}
