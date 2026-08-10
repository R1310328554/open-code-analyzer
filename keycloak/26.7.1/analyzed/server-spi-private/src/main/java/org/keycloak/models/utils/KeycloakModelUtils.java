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

package org.keycloak.models.utils;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.spec.SecretKeySpec;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.ConfigConstants;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.cache.AlternativeLookupProvider;
import org.keycloak.common.util.CertificateUtils;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.PemUtils;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.constants.OID4VCIConstants;
import org.keycloak.deployment.DeployedConfigurationsManager;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSecretConstants;
import org.keycloak.models.Constants;
import org.keycloak.models.GroupModel;
import org.keycloak.models.GroupProvider;
import org.keycloak.models.GroupProviderFactory;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.KeycloakSessionTaskWithResult;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.ScopeContainerModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.AccessToken.Access;
import org.keycloak.representations.idm.CertificateRepresentation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.keycloak.transaction.JtaTransactionManagerLookup;
import org.keycloak.transaction.RequestContextHelper;
import org.keycloak.utils.KeycloakSessionUtil;

import org.jboss.logging.Logger;

import static org.keycloak.utils.StreamsUtil.closing;

/**
 * 模型层通用辅助方法集合：ID 生成、角色/组解析、事务包装、认证流遍历等。
 * <p>供 JPA、Infinispan 等存储实现及 SPI 扩展复用。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>,
 * <a href="mailto:daniel.fesenmeyer@bosch.io">Daniel Fesenmeyer</a>
 */
public final class KeycloakModelUtils {

    private static final Logger logger = Logger.getLogger(KeycloakModelUtils.class);

    /** 客户端认证类型：client-secret。 */
    public static final String AUTH_TYPE_CLIENT_SECRET = "client-secret";
    /** 客户端认证类型：client-secret-jwt。 */
    public static final String AUTH_TYPE_CLIENT_SECRET_JWT = "client-secret-jwt";

    /** 组路径分隔符。 */
    public static final String GROUP_PATH_SEPARATOR = "/";
    /** 组路径中斜杠转义前缀。 */
    public static final String GROUP_PATH_ESCAPE = "~";
    /** 客户端角色名与 realm 角色名的分隔符（{@code clientId.roleName}）。 */
    public static final char CLIENT_ROLE_SEPARATOR = '.';

    public static final int MAX_CLIENT_LOOKUPS_DURING_ROLE_RESOLVE = 25;

    public static final int DEFAULT_RSA_KEY_SIZE = 4096;
    public static final int DEFAULT_CERTIFICATE_VALIDITY_YEARS = 3;

    private static final ThreadLocal<Integer> timeouts = new ThreadLocal<Integer>();

    private KeycloakModelUtils() {
    }

    /**
     * 使用 UUID 生成 36 字符标准 ID。
     * @return UUID.toString 形式的 ID
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成 22 字符短 ID：UUID 的 msb+lsb 经 Base64 URL 无填充编码。
     * @return 22 字符短 ID
     */
    public static String generateShortId() {
        return generateShortId(UUID.randomUUID());
    }

    /**
     * 将给定 UUID 编码为 22 字符 Base64 URL 短 ID。
     * @param uuid 待编码的 UUID
     * @return 22 字符字符串
     */
    public static String generateShortId(final UUID uuid) {
        final byte[] bytes = new byte[2 * Long.BYTES];
        // first the msb
        long l = uuid.getMostSignificantBits();
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            bytes[i] = (byte) (l & 0xff);
            l >>= 8;
        }
        // second the lsb
        l = uuid.getLeastSignificantBits();
        for (int i = Long.BYTES - 1; i >= 0; i--) {
            bytes[Long.BYTES + i] = (byte) (l & 0xff);
            l >>= 8;
        }
        // encode in base64 URL no padding (22 chars)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 校验字符串是否为合法 UUID。
     * @param uuid 待校验字符串
     * @return 合法时返回 {@code true}
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null) {
            return false;
        }
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static PublicKey getPublicKey(String publicKeyPem) {
        if (publicKeyPem != null) {
            try {
                return PemUtils.decodePublicKey(publicKeyPem);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    public static X509Certificate getCertificate(String cert) {
        if (cert != null) {
            try {
                return PemUtils.decodeCertificate(cert);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }


    public static PrivateKey getPrivateKey(String privateKeyPem) {
        if (privateKeyPem != null) {
            try {
                return PemUtils.decodePrivateKey(privateKeyPem);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static Key getSecretKey(String secret) {
        return secret != null ? new SecretKeySpec(secret.getBytes(), "HmacSHA256") : null;
    }

    public static String getPemFromKey(Key key) {
        return PemUtils.encodeKey(key);
    }

    public static String getPemFromCertificate(X509Certificate certificate) {
        return PemUtils.encodeCertificate(certificate);
    }

    public static CertificateRepresentation generateKeyPairCertificate(String subject) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, DEFAULT_CERTIFICATE_VALIDITY_YEARS);
        return generateKeyPairCertificate(subject, DEFAULT_RSA_KEY_SIZE, calendar);
    }

    public static CertificateRepresentation generateKeyPairCertificate(String subject, int keysize, Calendar endDate) {
        KeyPair keyPair = KeyUtils.generateRsaKeyPair(keysize);
        X509Certificate certificate = CertificateUtils.generateV1SelfSignedCertificate(keyPair, subject, BigInteger.valueOf(System.currentTimeMillis()), endDate.getTime());

        String privateKeyPem = PemUtils.encodeKey(keyPair.getPrivate());
        String certPem = PemUtils.encodeCertificate(certificate);

        CertificateRepresentation rep = new CertificateRepresentation();
        rep.setPrivateKey(privateKeyPem);
        rep.setCertificate(certPem);
        return rep;
    }

    public static String generateSecret(ClientModel client) {
        int secretLength = getRequiredClientSecretLength();
        String secret = SecretGenerator.getInstance().randomString(secretLength);
        client.setSecret(secret);
        client.setAttribute(ClientSecretConstants.CLIENT_SECRET_CREATION_TIME, String.valueOf(Time.currentTime()));
        return secret;
    }

    /**
     * 返回客户端密钥所需长度（字母数字字符数）。
     * <p>按 HS512 级熵生成，覆盖所有 HMAC 签名场景。</p>
     */
    public static int getRequiredClientSecretLength() {
        return SecretGenerator.equivalentEntropySize(SecretGenerator.SECRET_LENGTH_512_BITS, SecretGenerator.ALPHANUM.length);
    }

    public static String getDefaultClientAuthenticatorType() {
        return AUTH_TYPE_CLIENT_SECRET;
    }

    public static String generateCodeSecret() {
        return UUID.randomUUID().toString();
    }

    public static ClientModel createManagementClient(RealmModel realm, String name) {
        ClientModel client = createClient(realm, name);

        client.setBearerOnly(true);

        return client;
    }

    public static ClientModel createPublicClient(RealmModel realm, String name) {
        ClientModel client = createClient(realm, name);

        client.setPublicClient(true);

        return client;
    }

    private static ClientModel createClient(RealmModel realm, String name) {
        ClientModel client = realm.addClient(name);

        client.setClientAuthenticatorType(getDefaultClientAuthenticatorType());

        return client;
    }

    /**
     * 深度搜索：判断 role 是否为 composite 的子孙角色。
     *
     * @param role 待检查角色
     * @param composite 复合角色
     * @param visited 已访问角色集合（递归防环）
     * @return role 是 composite 后代时返回 {@code true}
     */
    public static boolean searchFor(RoleModel role, RoleModel composite, Set<String> visited) {
        if (visited.contains(composite.getId())) {
            return false;
        }

        visited.add(composite.getId());

        if (!composite.isComposite()) {
            return false;
        }

        Set<RoleModel> compositeRoles = composite.getCompositesStream().collect(Collectors.toSet());
        return compositeRoles.contains(role) ||
                compositeRoles.stream().anyMatch(x -> x.isComposite() && searchFor(role, x, visited));
    }

    /**
     * 启用「邮箱登录」且用户名含 {@code @} 时优先按邮箱查找用户；
     * 否则或未找到时按用户名查找。
     *
     * @param realm the realm to search within
     * @param username the username or email of the user
     * @return the found user if present; otherwise, {@code null}
     */
    public static UserModel findUserByNameOrEmail(KeycloakSession session, RealmModel realm, String username) {
        if (realm.isLoginWithEmailAllowed() && username.indexOf('@') != -1) {
            UserModel user = session.users().getUserByEmail(realm, username);
            if (user != null) {
                return user;
            }
        }

        return session.users().getUserByUsername(realm, username);
    }

    /**
     * 在 Keycloak 事务中执行给定任务。
     * @param factory 会话工厂
     * @param task 待执行任务
     */
    public static void runJobInTransaction(KeycloakSessionFactory factory, KeycloakSessionTask task) {
        runJobInTransaction(factory, null, task);
    }

    /**
     * 在 Keycloak 事务中执行任务，并传播上一会话的上下文。
     * @param factory 会话工厂
     * @param context 上一会话上下文
     * @param task 待执行任务
     */
    public static void runJobInTransaction(KeycloakSessionFactory factory, KeycloakContext context, KeycloakSessionTask task) {
        runJobInTransactionWithResult(factory, context, session -> {
            task.run(session);
            return null;
        }, task.getTaskName());
    }

    /**
     * 将原上下文的 realm 信息克隆到新会话。
     *
     * @param origContext 原始上下文
     * @param targetSession 目标会话
     */
    public static void cloneContextRealmClientToSession(final KeycloakContext origContext, final KeycloakSession targetSession) {
        cloneContextToSession(origContext, targetSession, false);
    }

    /**
     * 将原上下文的 realm、客户端与认证会话克隆到新会话。
     *
     * @param origContext 原始上下文
     * @param targetSession 目标会话
     */
    public static void cloneContextRealmClientSessionToSession(final KeycloakContext origContext, final KeycloakSession targetSession) {
        cloneContextToSession(origContext, targetSession, true);
    }

    /**
     * Sets up the context for the specified session.The original realm's context is used to
     * determine what models need to be re-loaded using the current session. The models
     * in the context are re-read from the new session via the IDs.
     */
    private static void cloneContextToSession(final KeycloakContext origContext, final KeycloakSession targetSession,
            final boolean includeAuthenticatedSessionModel) {
        if (origContext == null) {
            return;
        }

        // setup realm model if necessary.
        RealmModel realmModel = null;
        if (origContext.getRealm() != null) {
            realmModel = targetSession.realms().getRealm(origContext.getRealm().getId());
            if (realmModel != null) {
                targetSession.getContext().setRealm(realmModel);
            }
        }

        // setup client model if necessary.
        ClientModel clientModel = null;
        if (origContext.getClient() != null) {
            if (origContext.getRealm() == null || !Objects.equals(origContext.getRealm().getId(), origContext.getClient().getRealm().getId())) {
                realmModel = targetSession.realms().getRealm(origContext.getClient().getRealm().getId());
            }
            if (realmModel != null) {
                clientModel = targetSession.clients().getClientById(realmModel, origContext.getClient().getId());
                if (clientModel != null) {
                    targetSession.getContext().setClient(clientModel);
                }
            }
        }

        // setup auth session model if necessary.
        if (includeAuthenticatedSessionModel && origContext.getAuthenticationSession() != null) {
            if (origContext.getClient() == null || !Objects.equals(origContext.getClient().getId(), origContext.getAuthenticationSession().getClient().getId())) {
                realmModel = (origContext.getRealm() == null || !Objects.equals(origContext.getRealm().getId(), origContext.getAuthenticationSession().getRealm().getId()))
                        ? targetSession.realms().getRealm(origContext.getAuthenticationSession().getRealm().getId())
                        : targetSession.getContext().getRealm();
                clientModel = (realmModel != null)
                        ? targetSession.clients().getClientById(realmModel, origContext.getAuthenticationSession().getClient().getId())
                        : null;
            }
            if (clientModel != null) {
                RootAuthenticationSessionModel rootAuthSession = targetSession.authenticationSessions().getRootAuthenticationSession(
                        realmModel, origContext.getAuthenticationSession().getParentSession().getId());
                if (rootAuthSession != null) {
                    AuthenticationSessionModel authSessionModel = rootAuthSession.getAuthenticationSession(clientModel,
                            origContext.getAuthenticationSession().getTabId());
                    if (authSessionModel != null) {
                        targetSession.getContext().setAuthenticationSession(authSessionModel);
                    }
                }
            }
        }
    }

    /**
     * 在 Keycloak 事务中执行 Callable 并返回结果。
     * @param <V> 返回值类型
     * @param factory 会话工厂
     * @param callable 待执行 Callable
     * @return Callable 返回值
     */
    public static <V> V runJobInTransactionWithResult(KeycloakSessionFactory factory, final KeycloakSessionTaskWithResult<V> callable) {
        return runJobInTransactionWithResult(factory, null, callable, "Non-HTTP task");
    }

    /**
     * Wrap a given callable job into a KeycloakTransaction.
     * @param <V> The type for the result
     * @param factory The session factory
     * @param context The context from the previous session to use
     * @param callable The callable to execute
     * @param taskName Name of the task. Can be useful for logging purposes
     * @return The return value from the callable
     */
    public static <V> V runJobInTransactionWithResult(KeycloakSessionFactory factory, KeycloakContext context, final KeycloakSessionTaskWithResult<V> callable,
                                                      String taskName) {
        V result;
        KeycloakSession existing = KeycloakSessionUtil.getKeycloakSession();
        try (KeycloakSession session = factory.create()) {
            RequestContextHelper.getContext(session).setContextMessage(taskName);
            session.getTransactionManager().begin();
            KeycloakSessionUtil.setKeycloakSession(session);
            try {
                cloneContextRealmClientToSession(context, session);
                result = callable.run(session);
            } catch (Throwable t) {
                session.getTransactionManager().setRollbackOnly();
                throw t;
            }
        } finally {
            KeycloakSessionUtil.setKeycloakSession(existing);
        }
        return result;
    }

    /**
     * Wrap given runnable job into KeycloakTransaction. Set custom timeout for the JTA transaction (in case we're in the environment with JTA enabled)
     *
     * @param factory
     * @param task
     * @param timeoutInSeconds
     */
    public static void runJobInTransactionWithTimeout(KeycloakSessionFactory factory, KeycloakSessionTask task, int timeoutInSeconds) {
        try {
            setTransactionLimit(factory, timeoutInSeconds);
            runJobInTransaction(factory, task);
        } finally {
            setTransactionLimit(factory, 0);
        }

    }

    public static void setTransactionLimit(KeycloakSessionFactory factory, int timeoutInSeconds) {
        JtaTransactionManagerLookup lookup = (JtaTransactionManagerLookup) factory.getProviderFactory(JtaTransactionManagerLookup.class);
        if (lookup != null) {
            if (lookup.getTransactionManager() != null) {
                try {
                    // If timeout is set to 0, reset to default transaction timeout
                    lookup.getTransactionManager().setTransactionTimeout(timeoutInSeconds);

                    if (timeoutInSeconds == 0) {
                        timeouts.remove();
                    } else {
                        timeouts.set(timeoutInSeconds);
                    }
                } catch (SystemException e) {
                    // Shouldn't happen for Wildfly transaction manager
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static Optional<Integer> getTransactionLimit() {
        return Optional.ofNullable(timeouts.get());
    }

    public static Function<KeycloakSessionFactory, ComponentModel> componentModelGetter(String realmId, String componentId) {
        return factory -> getComponentModel(factory, realmId, componentId);
    }

    public static ComponentModel getComponentModel(KeycloakSessionFactory factory, String realmId, String componentId) {
        AtomicReference<ComponentModel> cm = new AtomicReference<>();
        KeycloakModelUtils.runJobInTransaction(factory, session -> {
            RealmModel realm = session.realms().getRealm(realmId);
            cm.set(realm == null ? null : realm.getComponent(componentId));
        });
        return cm.get();
    }

    public static <T extends Provider> ProviderFactory<T> getComponentFactory(KeycloakSessionFactory factory, Class<T> providerClass, Scope config, String spiName) {
        String realmId = config.get("realmId");
        String componentId = config.get("componentId");
        if (realmId == null || componentId == null) {
            realmId = "ROOT";
            ComponentModel cm = new ScopeComponentModel(providerClass, config, spiName, realmId);
            return factory.getProviderFactory(providerClass, realmId, cm.getId(), k -> cm);
        } else {
            return factory.getProviderFactory(providerClass, realmId, componentId, componentModelGetter(realmId, componentId));
        }
    }

    private static class ScopeComponentModel extends ComponentModel {

        private final String componentId;
        private final String providerId;
        private final String providerType;
        private final String realmId;
        private final Scope config;

        public ScopeComponentModel(Class<?> providerClass, Scope baseConfiguration, String spiName, String realmId) {
            final String pr = baseConfiguration.get("provider", Config.getProvider(spiName));

            this.providerId = pr == null ? "default" : pr;
            this.config = baseConfiguration.scope(this.providerId);
            this.componentId = spiName + "- " + (realmId == null ? "" : "f:" + realmId + ":") + this.providerId;
            this.realmId = realmId;
            this.providerType = providerClass.getName();
        }

        @Override
        public String getProviderType() {
            return providerType;
        }

        @Override
        public String getProviderId() {
            return providerId;
        }

        @Override
        public String getName() {
            return componentId + "-config";
        }

        @Override
        public String getId() {
            return componentId;
        }

        @Override
        public String getParentId() {
            return realmId;
        }

        @Override
        public boolean get(String key, boolean defaultValue) {
            return config.getBoolean(key, defaultValue);
        }

        @Override
        public long get(String key, long defaultValue) {
            return config.getLong(key, defaultValue);
        }

        @Override
        public int get(String key, int defaultValue) {
            return config.getInt(key, defaultValue);
        }

        @Override
        public String get(String key, String defaultValue) {
            return config.get(key, defaultValue);
        }

        @Override
        public String get(String key) {
            return get(key, null);
        }
    }

    public static String getMasterRealmAdminManagementClientId(String realmName) {
        return realmName + "-realm";
    }

    // USER FEDERATION RELATED STUFF


    public static ComponentModel createComponentModel(String name, String parentId, String providerId, String providerType, String... config) {
        ComponentModel mapperModel = new ComponentModel();
        mapperModel.setParentId(parentId);
        mapperModel.setName(name);
        mapperModel.setProviderId(providerId);
        mapperModel.setProviderType(providerType);

        String key = null;
        for (String configEntry : config) {
            if (key == null) {
                key = configEntry;
            } else {
                mapperModel.getConfig().add(key, configEntry);
                key = null;
            }
        }
        if (key != null) {
            throw new IllegalStateException("Invalid count of arguments for config. Maybe mistake?");
        }

        return mapperModel;
    }


    // END USER FEDERATION RELATED STUFF

    public static String toLowerCaseSafe(String str) {
        return str == null ? null : str.toLowerCase();
    }

    /**
     * 为 realm 创建指定名称的默认角色并关联。
     *
     * @param realm Realm
     * @param defaultRoleName 默认角色名称
     */
    public static void setupDefaultRole(RealmModel realm, String defaultRoleName) {
        RoleModel defaultRole = realm.addRole(defaultRoleName);
        defaultRole.setDescription("${role_default-roles}");
        realm.setDefaultRole(defaultRole);
    }

    public static RoleModel setupOfflineRole(RealmModel realm) {
        RoleModel offlineRole = realm.getRole(Constants.OFFLINE_ACCESS_ROLE);

        if (offlineRole == null) {
            offlineRole = realm.addRole(Constants.OFFLINE_ACCESS_ROLE);
            offlineRole.setDescription("${role_offline-access}");
            realm.addToDefaultRoles(offlineRole);
        }

        return offlineRole;
    }

    public static void setupDeleteAccount(ClientModel accountClient) {
        RoleModel deleteOwnAccount = accountClient.getRole(AccountRoles.DELETE_ACCOUNT);
        if (deleteOwnAccount == null) {
            deleteOwnAccount = accountClient.addRole(AccountRoles.DELETE_ACCOUNT);
        }
        deleteOwnAccount.setDescription("${role_" + AccountRoles.DELETE_ACCOUNT + "}");
    }

    /**
     * 递归收集认证流及其子流中的全部 {@link AuthenticationExecutionModel}。
     *
     * @param realm realm
     * @param flow 认证流
     * @param result 输入为空列表，执行后填入全部 execution
     */
    public static void deepFindAuthenticationExecutions(RealmModel realm, AuthenticationFlowModel flow, List<AuthenticationExecutionModel> result) {
        realm.getAuthenticationExecutionsStream(flow.getId()).forEachOrdered(execution -> {
            if (execution.isAuthenticatorFlow()) {
                AuthenticationFlowModel subFlow = realm.getAuthenticationFlowById(execution.getFlowId());
                deepFindAuthenticationExecutions(realm, subFlow, result);
            } else {
                result.add(execution);
            }
        });
    }

    public static Collection<String> resolveAttribute(GroupModel group, String name, boolean aggregateAttrs) {
        Set<String> values = group.getAttributeStream(name).collect(Collectors.toSet());
        if ((values.isEmpty() || aggregateAttrs) && group.getParentId() != null) {
            values.addAll(resolveAttribute(group.getParent(), name, aggregateAttrs));
        }
        return values;
    }

    public static Collection<String> resolveAttribute(UserModel user, String name, boolean aggregateAttrs) {
        List<String> values = user.getAttributeStream(name).collect(Collectors.toList());
        Set<String> aggrValues = new HashSet<>();
        if (!values.isEmpty()) {
            if (!aggregateAttrs) {
                return values;
            }
            aggrValues.addAll(values);
        }
        Stream<Collection<String>> attributes = user.getGroupsStream()
                .map(group -> resolveAttribute(group, name, aggregateAttrs))
                .filter(attr -> !attr.isEmpty());

        if (!aggregateAttrs) {
            Optional<Collection<String>> first = attributes.findFirst();
            if (first.isPresent()) return first.get();
        } else {
            aggrValues.addAll(attributes.flatMap(Collection::stream).collect(Collectors.toSet()));
        }

        return aggrValues;
    }


    /**
     * 从会话读取组路径是否应对斜杠转义。
     * @param session 会话
     * @return 需要转义时返回 {@code true}
     */
    public static boolean escapeSlashesInGroupPath(KeycloakSession session) {
        GroupProviderFactory<?> fact = (GroupProviderFactory<?>) session.getKeycloakSessionFactory().getProviderFactory(GroupProvider.class);
        return fact.escapeSlashesInGroupPath();
    }

    /**
     * 按路径查找组，路径以 {@code /} 分隔，支持组名含斜杠及转义。
     * <p>例如 {@code /parent\/group/child} 表示 ["parent/group", "child"] 两级路径。</p>
     *
     * @param session Keycloak session
     * @param realm The realm
     * @param path Path that will be searched among groups
     *
     * @return {@code GroupModel} corresponding to the given {@code path} or {@code null} if no group was found
     */
    public static GroupModel findGroupByPath(KeycloakSession session, RealmModel realm, String path) {
        if (path == null) {
            return null;
        }
        String[] split = splitPath(path, escapeSlashesInGroupPath(session));
        if (split.length == 0) return null;
        return getGroupModel(session, realm, null, split, 0);
    }

    /**
     * 按已拆分的组名路径数组查找组。
     *
     * @param session Keycloak session
     * @param realm The realm
     * @param path 组名路径层级
     *
     * @return {@code GroupModel} corresponding to the given {@code path} or {@code null} if no group was found
     */
    public static GroupModel findGroupByPath(KeycloakSession session, RealmModel realm, String[] path) {
        if (path == null || path.length == 0) {
            return null;
        }
        return getGroupModel(session, realm, null, path, 0);
    }

    private static GroupModel getGroupModel(KeycloakSession session, RealmModel realm, GroupModel parent, String[] split, int index) {
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = index; i < split.length; i++) {
            nameBuilder.append(split[i]);

            GroupModel group;
            if (parent != null && GroupModel.Type.ORGANIZATION.equals(parent.getType())) {
                // For organization groups, use OrganizationProvider.searchGroupsByName
                OrganizationModel org = parent.getOrganization();
                if (org == null) {
                    return null;
                }
                OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
                if (orgProvider == null) {
                    // Organization feature disabled, cannot resolve organization group paths
                    return null;
                }
                String parentId = parent.getId();
                group = orgProvider.searchGroupsByName(org, nameBuilder.toString(), true, null, null)
                    .filter(g -> parentId.equals(g.getParentId()))
                    .findFirst()
                    .orElse(null);
            } else {
                // For realm groups (or null parent), use GroupProvider.getGroupByName
                group = session.groups().getGroupByName(realm, parent, nameBuilder.toString());
            }

            if (group != null) {
                if (i < split.length-1) {
                    return getGroupModel(session, realm, group, split, i+1);
                } else {
                    return group;
                }
            }
            nameBuilder.append(GROUP_PATH_SEPARATOR);
        }
        return null;
    }

    /**
     * Splits a group path than can be escaped for slashes.
     * @param path The group path
     * @param escapedSlashes true if slashes are escaped in the path
     * @return
     */
    public static String[] splitPath(String path, boolean escapedSlashes) {
        if (path == null) {
            return null;
        }
        if (path.startsWith(GROUP_PATH_SEPARATOR)) {
            path = path.substring(1);
        }
        if (path.endsWith(GROUP_PATH_SEPARATOR)) {
            path = path.substring(0, path.length() - 1);
        }
        // just split by slashed that are not escaped
        return escapedSlashes
                ? Arrays.stream(path.split("(?<!" + Pattern.quote(GROUP_PATH_ESCAPE) + ")" + Pattern.quote(GROUP_PATH_SEPARATOR)))
                        .map(KeycloakModelUtils::unescapeGroupNameForPath)
                        .toArray(String[]::new)
                : path.split(GROUP_PATH_SEPARATOR);
    }

    /**
     * Escapes the slash in the name if found. "group/slash" returns "group\/slash".
     * @param groupName
     * @return
     */
    private static String escapeGroupNameForPath(String groupName) {
        return groupName.replace(GROUP_PATH_SEPARATOR, GROUP_PATH_ESCAPE + GROUP_PATH_SEPARATOR);
    }

    /**
     * Unescape the escaped slashes in name. "group\/slash" returns "group/slash".
     * @param groupName
     * @return
     */
    private static String unescapeGroupNameForPath(String groupName) {
        return groupName.replace(GROUP_PATH_ESCAPE + GROUP_PATH_SEPARATOR, GROUP_PATH_SEPARATOR);
    }

    public static String buildGroupPath(boolean escapeSlashes, String... names) {
        StringBuilder sb = new StringBuilder();
        sb.append(GROUP_PATH_SEPARATOR);
        for (int i = 0; i < names.length; i++) {
            sb.append(escapeSlashes? escapeGroupNameForPath(names[i]) : names[i]);
            if (i < names.length - 1) {
                sb.append(GROUP_PATH_SEPARATOR);
            }
        }
        return sb.toString();
    }

    /**
     * Unified recursive helper to build group paths for both realm and organization groups.
     * For organization groups, stops recursion at the internal organization group (whose name equals the org UUID).
     * For realm groups, organizationId is null so recursion continues to the root.
     */
    private static void buildGroupPath(StringBuilder sb, String groupName, GroupModel parent, boolean escapeSlashes, String organizationId) {
        if (parent != null) {
            // For org groups: stop recursion at internal org group (name equals org UUID)
            // For realm groups: organizationId is null, so this check never triggers
            if (organizationId == null || !organizationId.equals(parent.getName())) {
                buildGroupPath(sb, parent.getName(), parent.getParent(), escapeSlashes, organizationId);
            }
        }
        sb.append(GROUP_PATH_SEPARATOR).append(escapeSlashes ? escapeGroupNameForPath(groupName) : groupName);
    }

    public static String buildGroupPath(GroupModel group) {
        if (group == null) return null;

        StringBuilder sb = new StringBuilder();
        buildGroupPath(sb, group.getName(), group.getParent(), group.escapeSlashesInGroupPath(), getOrgId(group));
        return sb.toString();
    }

    public static String buildGroupPath(GroupModel group, GroupModel otherParentGroup) {
        if (group == null) return null;

        StringBuilder sb = new StringBuilder();
        buildGroupPath(sb, group.getName(), otherParentGroup, group.escapeSlashesInGroupPath(), getOrgId(group));
        return sb.toString();
    }

    private static String getOrgId(GroupModel group) {
        OrganizationModel organization = group.getOrganization();
        return organization != null ? organization.getId() : null;
    }

    public static String normalizeGroupPath(final String groupPath) {
        if (groupPath == null) {
            return null;
        }

        String normalized = groupPath;

        if (!normalized.startsWith(GROUP_PATH_SEPARATOR)) {
            normalized = GROUP_PATH_SEPARATOR +  normalized;
        }
        if (normalized.endsWith(GROUP_PATH_SEPARATOR)) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    /**
     * Finds an organization group by its user-friendly path (without the organization UUID prefix).
     * <p />
     * This method searches for a group within the specified organization using a path that does not include
     * the internal organization group UUID. For example, to find a group at internal path
     * {@code /8855824f-3b7b-4f49-ac80-8777d547c9fb/MyGroupName/lvl2}, you would pass {@code /MyGroupName/lvl2}
     * as the path parameter.
     * <p />
     * The organization context is used to determine the internal organization group, and the search is performed
     * relative to that group.
     *
     * @param session the Keycloak session
     * @param realm the realm
     * @param organization the organization context
     * @param path the user-facing path (without organization UUID prefix)
     * @return the {@code GroupModel} corresponding to the given path within the organization, or {@code null} if not found
     */
    public static GroupModel findGroupByPath(KeycloakSession session, RealmModel realm, OrganizationModel organization, String path) {
        if (path == null || organization == null) {
            return null;
        }

        OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
        GroupModel orgInternalGroup = orgProvider.getOrganizationGroup(organization);

        // Split the path
        String[] split = splitPath(path, escapeSlashesInGroupPath(session));
        if (split.length == 0) {
            return null;
        }

        // Search for the group starting from the internal organization group as parent
        return getGroupModel(session, realm, orgInternalGroup, split, 0);
    }

    /**
     * Validates and retrieves the organization for an Identity Provider mapper.
     * This performs all necessary checks to ensure the IdP-organization relationship is valid:
     * - Organizations feature is enabled
     * - Organization exists and is enabled
     * - Bidirectional link exists (organization still has this IdP)
     *
     * @param session the Keycloak session
     * @param idpModel the identity provider model
     * @return the validated organization if all checks pass, null otherwise
     */
    public static OrganizationModel getOrganizationForIdpMapper(KeycloakSession session, IdentityProviderModel idpModel) {
        String idpOrgId = idpModel.getOrganizationId();
        if (idpOrgId == null) {
            return null;
        }

        OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
        if (orgProvider != null && orgProvider.isEnabled()) {
            OrganizationModel organization = orgProvider.getById(idpOrgId);

            if (organization != null && organization.isEnabled() && organization.getIdentityProviders().anyMatch(idp -> idp.getAlias().equals(idpModel.getAlias()))) {
                return organization;
            }
        }

        logger.warnf("Cannot obtain organization '%s' linked to IdP '%s'", idpModel.getAlias(), idpOrgId);

        return null;
    }

    /**
     * Retrieves and validates a group for use in an Identity Provider mapper.
     * The lookup strategy is determined by the {@code groupType} config value:
     * <ul>
     *   <li>{@code "ORGANIZATION"} — searches within the organization groups linked to the IdP</li>
     *   <li>{@code "REALM"} or missing — searches realm groups</li>
     * </ul>
     *
     * @param session the Keycloak session
     * @param realm the realm
     * @param mapperModel the mapper model configuration containing the group path and group type
     * @param context the brokered identity context containing the IdP configuration
     * @return the group if found and valid, null otherwise (mapper should be skipped)
     */
    public static GroupModel getGroupForIdpMapper(KeycloakSession session,
                                                   RealmModel realm,
                                                   IdentityProviderMapperModel mapperModel,
                                                   BrokeredIdentityContext context) {
        String groupPath = mapperModel.getConfig().get(ConfigConstants.GROUP);
        String groupTypeStr = mapperModel.getConfig().get(ConfigConstants.GROUP_TYPE);
        GroupModel group = null;

        // Parse the group type from config
        GroupModel.Type groupType = null;
        if (groupTypeStr != null) {
            try {
                groupType = GroupModel.Type.valueOf(groupTypeStr);
            } catch (IllegalArgumentException e) {
                // Invalid group type, treat as null
            }
        }

        if (groupType == GroupModel.Type.ORGANIZATION) {
            OrganizationModel organization = getOrganizationForIdpMapper(session, context.getIdpConfig());
            if (organization != null) {
                group = findGroupByPath(session, realm, organization, groupPath);
            }
        } else {
            // GroupModel.Type.REALM or null → search realm groups
            group = findGroupByPath(session, realm, groupPath);
        }

        if (group == null) {
            logger.warnf("Unable to find group by path '%s' referenced by mapper '%s' on realm '%s'.", groupPath, mapperModel.getName(), realm.getName());
            return null;
        }

        return group;
    }

    public static Stream<RoleModel> getClientScopeMappingsStream(ClientModel client, ScopeContainerModel container) {
        return container.getScopeMappingsStream()
                .filter(role -> role.getContainer() instanceof ClientModel &&
                        Objects.equals(client.getId(), role.getContainer().getId()));
    }

    /**
     * @deprecated 将移除，请改用 {@link #getRoleFromString(KeycloakSession, RealmModel, String)}。
     */
    @Deprecated(forRemoval = true, since = "26.6")
    public static RoleModel getRoleFromString(RealmModel realm, String roleName) {
        return getRoleFromString(KeycloakSessionUtil.getKeycloakSession(), realm, roleName);
    }

    public static RoleModel getRoleFromString(KeycloakSession session, RealmModel realm, String roleName) {
        if (session == null) {
            return getRoleFromStringNoCaching(realm, roleName);
        }
        return session.getProvider(AlternativeLookupProvider.class)
                .lookupRoleFromString(realm, roleName);
    }

    // Used in various role mappers
    private static RoleModel getRoleFromStringNoCaching(RealmModel realm, String roleName) {
        if (roleName == null) {
            return null;
        }

        // Check client roles for all possible splits by dot
        int counter = 0;
        int scopeIndex = roleName.lastIndexOf(CLIENT_ROLE_SEPARATOR);
        while (scopeIndex >= 0 && counter < MAX_CLIENT_LOOKUPS_DURING_ROLE_RESOLVE) {
            counter++;
            String appName = roleName.substring(0, scopeIndex);
            ClientModel client = realm.getClientByClientId(appName);
            if (client != null) {
                String role = roleName.substring(scopeIndex + 1);
                return client.getRole(role);
            }

            scopeIndex = roleName.lastIndexOf(CLIENT_ROLE_SEPARATOR, scopeIndex - 1);
        }
        if (counter >= MAX_CLIENT_LOOKUPS_DURING_ROLE_RESOLVE) {
            logger.warnf("Not able to retrieve role model from the role name '%s'. Please use shorter role names with the limited amount of dots, roleName", roleName.length() > 100 ? roleName.substring(0, 100) + "..." : roleName);
            return null;
        }

        // determine if roleName is a realm role
        return realm.getRole(roleName);
    }

    // Used for hardcoded role mappers
    public static String[] parseRole(String role) {
        int scopeIndex = role.lastIndexOf(CLIENT_ROLE_SEPARATOR);
        if (scopeIndex > -1) {
            String appName = role.substring(0, scopeIndex);
            role = role.substring(scopeIndex + 1);
            return new String[]{appName, role};
        } else {
            return new String[]{null, role};

        }
    }

    public static String buildRoleQualifier(String clientId, String roleName) {
        if (clientId == null) {
            return roleName;
        }

        return clientId + CLIENT_ROLE_SEPARATOR + roleName;
    }

    public static RoleModel getRoleByName(RealmModel realm, String clientId, String name) {
        if (clientId == null) {
            return realm.getRole(name);
        } else {
            ClientModel client = realm.getClientByClientId(clientId);

            if (client == null) {
                return null;
            }

            return client.getRole(name);
        }
    }

    public static void removeTransientAdminRoles(RealmModel realm, String clientId, UserModel user, Access access) {
        if (access == null || access.getRoles() == null) {
            return;
        }

        Set<String> roles = access.getRoles();
        Iterator<String> roleIterator = roles.iterator();

        while (roleIterator.hasNext()) {
            String role = roleIterator.next();
            RoleModel adminRole = getRoleByName(realm, clientId, role);

            if (AdminRoles.containsAdminRole(adminRole) && !user.hasRole(adminRole)) {
                roleIterator.remove();
            }
        }
    }

    /**
     * 检查认证流是否正被 realm 引用（浏览器流、注册流等）。
     *
     * @param realm realm
     * @param model 认证流
     * @return 使用中返回 {@code true}
     */
    public static boolean isFlowUsed(KeycloakSession session, RealmModel realm, AuthenticationFlowModel model) {
        AuthenticationFlowModel realmFlow = null;

        if ((realmFlow = realm.getBrowserFlow()) != null && realmFlow.getId().equals(model.getId())) return true;
        if ((realmFlow = realm.getRegistrationFlow()) != null && realmFlow.getId().equals(model.getId())) return true;
        if ((realmFlow = realm.getClientAuthenticationFlow()) != null && realmFlow.getId().equals(model.getId())) return true;
        if ((realmFlow = realm.getDirectGrantFlow()) != null && realmFlow.getId().equals(model.getId())) return true;
        if ((realmFlow = realm.getResetCredentialsFlow()) != null && realmFlow.getId().equals(model.getId())) return true;
        if ((realmFlow = realm.getDockerAuthenticationFlow()) != null && realmFlow.getId().equals(model.getId())) return true;
        if ((realmFlow = realm.getFirstBrokerLoginFlow()) != null && realmFlow.getId().equals(model.getId())) return true;

        Stream<ClientModel> browserFlowOverridingClients = realm.searchClientByAuthenticationFlowBindingOverrides(Collections.singletonMap("browser", model.getId()), 0, 1);
        Stream<ClientModel> directGrantFlowOverridingClients = realm.searchClientByAuthenticationFlowBindingOverrides(Collections.singletonMap("direct_grant", model.getId()), 0, 1);
        boolean usedByClient = closing(Stream.concat(browserFlowOverridingClients, directGrantFlowOverridingClients))
                .limit(1)
                .findAny()
                .isPresent();

        if (usedByClient) {
            return true;
        }

        return session.identityProviders().getByFlow(model.getId(), null,0, 1).findAny().isPresent();
    }

    /**
     * 递归删除认证流（含子流与 execution）。
     *
     * @param session Keycloak 会话
     * @param realm realm
     * @param authFlow 待删除流
     * @param flowUnavailableHandler 流/子流/执行器为 null 时的回调
     * @param builtinFlowHandler will be executed when flow is built-in flow
     */
    public static void deepDeleteAuthenticationFlow(KeycloakSession session, RealmModel realm, AuthenticationFlowModel authFlow, Runnable flowUnavailableHandler, Runnable builtinFlowHandler, boolean isParentBuiltInFlow) {
        if (authFlow == null) {
            flowUnavailableHandler.run();
            return;
        }
        if (isParentBuiltInFlow) {
            builtinFlowHandler.run();
        }

        realm.getAuthenticationExecutionsStream(authFlow.getId())
                .forEachOrdered(authExecutor -> deepDeleteAuthenticationExecutor(session, realm, authExecutor, flowUnavailableHandler, builtinFlowHandler, isParentBuiltInFlow));

        realm.removeAuthenticationFlow(authFlow);
    }

    /**
     * Recursively remove authentication executor (including sub-flows and configs) from the model storage
     *
     * @param session The keycloak session
     * @param realm The realm
     * @param authExecutor The authentication executor to remove
     * @param flowUnavailableHandler Handler that will be executed when flow, sub-flow or executor is null
     * @param builtinFlowHandler Handler that will be executed when flow is built-in flow
     */
    public static void deepDeleteAuthenticationExecutor(KeycloakSession session, RealmModel realm, AuthenticationExecutionModel authExecutor, Runnable flowUnavailableHandler, Runnable builtinFlowHandler, boolean isParentBuiltInFlow) {
        if (authExecutor == null) {
            flowUnavailableHandler.run();
            return;
        }

        // recursively remove sub flows
        if (authExecutor.getFlowId() != null) {
            AuthenticationFlowModel authFlow = realm.getAuthenticationFlowById(authExecutor.getFlowId());
            deepDeleteAuthenticationFlow(session, realm, authFlow, flowUnavailableHandler, builtinFlowHandler, isParentBuiltInFlow);
        }

        // remove the config if not shared
        if (authExecutor.getAuthenticatorConfig() != null) {
            DeployedConfigurationsManager configManager = new DeployedConfigurationsManager(session);
            if (configManager.getDeployedAuthenticatorConfig(authExecutor.getAuthenticatorConfig()) == null) {
                AuthenticatorConfigModel config = configManager.getAuthenticatorConfig(realm, authExecutor.getAuthenticatorConfig());
                if (config != null) {
                    realm.removeAuthenticatorConfig(config);
                }
            }
        }

        // remove the executor at the end
        realm.removeAuthenticatorExecution(authExecutor);
    }

    public static ClientScopeModel getClientScopeByName(RealmModel realm, String clientScopeName) {
        return realm.getClientScopesStream()
                .filter(clientScope -> Objects.equals(clientScopeName, clientScope.getName()))
                .findFirst()
                // check if we are referencing a client instead of a scope
                .orElseGet(() -> realm.getClientByClientId(clientScopeName));
    }

    /**
     * 按 ID 查找 ClientScope 或 Client（仅知 ID、不知类型时使用）。
     */
    public static ClientScopeModel findClientScopeById(RealmModel realm, ClientModel client, String clientScopeId) {
        if (client.getId().equals(clientScopeId)) {
            return client;
        }

        ClientScopeModel clientScope = realm.getClientScopeById(clientScopeId);

        if (clientScope == null) {
            // as fallback we try to resolve parameterized scopes
            clientScope = client.getParameterizedClientScope(clientScopeId);
        }

        if (clientScope != null) {
            return clientScope;
        } else {
            return realm.getClientById(clientScopeId);
        }
    }

    /** 将 scope 名称中的空格替换为下划线，便于作为 scope 参数值。 **/
    public static String convertClientScopeName(String previousName) {
        if (previousName.contains(" ")) {
            return previousName.replaceAll(" ", "_");
        } else {
            return previousName;
        }
    }

    public static void setupAuthorizationServices(RealmModel realm) {
        for (String roleName : Constants.AUTHZ_DEFAULT_AUTHORIZATION_ROLES) {
            if (realm.getRole(roleName) == null) {
                RoleModel role = realm.addRole(roleName);
                role.setDescription("${role_" + roleName + "}");
                realm.addToDefaultRoles(role);
            }
        }
    }

    public static void suspendJtaTransaction(KeycloakSessionFactory factory, Runnable runnable) {
        JtaTransactionManagerLookup lookup = (JtaTransactionManagerLookup) factory.getProviderFactory(JtaTransactionManagerLookup.class);
        Transaction suspended = null;
        try {
            if (lookup != null) {
                if (lookup.getTransactionManager() != null) {
                    try {
                        suspended = lookup.getTransactionManager().suspend();
                    } catch (SystemException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            runnable.run();
        } finally {
            if (suspended != null) {
                try {
                    lookup.getTransactionManager().resume(suspended);
                } catch (InvalidTransactionException | SystemException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

    public static String getIdentityProviderDisplayName(KeycloakSession session, IdentityProviderModel provider) {
        String displayName = provider.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }

        SocialIdentityProviderFactory<?> providerFactory = (SocialIdentityProviderFactory<?>) session.getKeycloakSessionFactory()
                .getProviderFactory(SocialIdentityProvider.class, provider.getProviderId());
        if (providerFactory != null) {
            return providerFactory.getName();
        } else {
            return provider.getAlias();
        }
    }

    /**
     * @param clientAuthenticatorType ignored, kept for backwards compatibility
     * @param signingAlg ignored, kept for backwards compatibility
     * @return secret size in alphanumeric characters with HS512-level entropy
     */
    public static int getSecretLengthByAuthenticationType(String clientAuthenticatorType, String signingAlg) {
        return getRequiredClientSecretLength();
    }

    /**
     * 为 realm 设置默认组。
     * @param session 会话
     * @param realm realm
     * @param groups 组路径列表
     * @throws RuntimeException 组不存在时抛出
     */
    public static void setDefaultGroups(KeycloakSession session, RealmModel realm, Stream<String> groups) {
        realm.getDefaultGroupsStream().toList().forEach(realm::removeDefaultGroup);
        groups.forEach(path -> {
            GroupModel found = KeycloakModelUtils.findGroupByPath(session, realm, path);
            if (found == null) throw new RuntimeException("default group in realm rep doesn't exist: " + path);
            realm.addDefaultGroup(found);
        });
    }

    /**
     * <p>Runs the given {@code operation} within the scope of the given @{target} realm.
     *
     * <p>Only use this method when you need to execute operations in a {@link RealmModel} object that is different
     * than the one associated with the {@code session}.
     *
     * @param session the session
     * @param target the target realm
     * @param operation the operation
     * @return the result from the supplier
     */
    public static <T> T runOnRealm(KeycloakSession session, RealmModel target, Function<KeycloakSession, T> operation) {
        KeycloakContext context = session.getContext();
        RealmModel currentRealm = context.getRealm();

        if (currentRealm.equals(target)) {
            return operation.apply(session);
        }

        try {
            context.setRealm(target);
            return operation.apply(session);
        } finally {
            context.setRealm(currentRealm);
        }
    }

    /** @return 给定客户端可接受的协议列表。 */
    
    public static List<String> getAcceptedClientScopeProtocols(ClientModel client) {
        List<String> acceptedClientProtocols;
        if (client.getProtocol() == null || "openid-connect".equals(client.getProtocol())) {
            acceptedClientProtocols = List.of("openid-connect", OID4VCIConstants.OID4VC_PROTOCOL);
        }else {
            acceptedClientProtocols = List.of(client.getProtocol());
        }
        return acceptedClientProtocols;
    }
}
