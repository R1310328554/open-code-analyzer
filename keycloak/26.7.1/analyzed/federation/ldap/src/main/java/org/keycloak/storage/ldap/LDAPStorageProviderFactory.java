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

package org.keycloak.storage.ldap;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.keycloak.Config;
import org.keycloak.common.constants.KerberosConstants;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.federation.kerberos.CommonKerberosConfig;
import org.keycloak.federation.kerberos.impl.KerberosServerSubjectAuthenticator;
import org.keycloak.federation.kerberos.impl.KerberosUsernamePasswordAuthenticator;
import org.keycloak.federation.kerberos.impl.SPNEGOAuthenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.UserCache;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.UserStorageUtil;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQueryConditionsBuilder;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore;
import org.keycloak.storage.ldap.kerberos.LDAPProviderKerberosConfig;
import org.keycloak.storage.ldap.mappers.FullNameLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.FullNameLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.HardcodedLDAPAttributeMapper;
import org.keycloak.storage.ldap.mappers.HardcodedLDAPAttributeMapperFactory;
import org.keycloak.storage.ldap.mappers.KerberosPrincipalAttributeMapperFactory;
import org.keycloak.storage.ldap.mappers.LDAPConfigDecorator;
import org.keycloak.storage.ldap.mappers.LDAPMappersComparator;
import org.keycloak.storage.ldap.mappers.LDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.UserAttributeLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.UserAttributeLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.msad.MSADUserAccountControlStorageMapper;
import org.keycloak.storage.ldap.mappers.msad.MSADUserAccountControlStorageMapperFactory;
import org.keycloak.storage.user.ImportSynchronization;
import org.keycloak.storage.user.SynchronizationResult;
import org.keycloak.utils.CredentialHelper;

import org.jboss.logging.Logger;

/**
 * LDAP 用户存储工厂：组件生命周期、配置校验、默认 mapper 创建及全量/增量同步。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class LDAPStorageProviderFactory implements UserStorageProviderFactory<LDAPStorageProvider>, ImportSynchronization {


    private static final Logger logger = Logger.getLogger(LDAPStorageProviderFactory.class);
    public static final String PROVIDER_NAME = LDAPConstants.LDAP_PROVIDER;
    private static final String LDAP_CONNECTION_POOL_PROTOCOL = "com.sun.jndi.ldap.connect.pool.protocol";
    private static final String SECURE_REFERRAL = "secureReferral";
    private static final boolean SECURE_REFERRAL_DEFAULT = true;

    private LDAPIdentityStoreRegistry ldapStoreRegistry;

    protected static final List<ProviderConfigProperty> configProperties;

    static {
        configProperties = getConfigProps(null);
    }

    private static List<ProviderConfigProperty> getConfigProps(ComponentModel parent) {
        boolean readOnly = false;
        if (parent != null) {
            LDAPConfig config = new LDAPConfig(parent.getConfig());
            readOnly = config.getEditMode() != UserStorageProvider.EditMode.WRITABLE;
        }


        return ProviderConfigurationBuilder.create()
                .property().name(LDAPConstants.EDIT_MODE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(UserStorageProviderModel.IMPORT_ENABLED)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("true")
                .add()
                .property().name(LDAPConstants.SYNC_REGISTRATIONS)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(LDAPConstants.VENDOR)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.USE_PASSWORD_MODIFY_EXTENDED_OP)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .add()
                .property().name(LDAPConstants.USERNAME_LDAP_ATTRIBUTE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.RDN_LDAP_ATTRIBUTE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.UUID_LDAP_ATTRIBUTE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.USER_OBJECT_CLASSES)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.CONNECTION_URL)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.USERS_DN)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.RELATIVE_CREATE_DN)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.AUTH_TYPE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("simple")
                .add()
                .property().name(LDAPConstants.START_TLS)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .add()
                .property().name(LDAPConstants.BIND_DN)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.BIND_CREDENTIAL)
                .type(ProviderConfigProperty.PASSWORD)
                .secret(true)
                .add()
                .property().name(LDAPConstants.CUSTOM_USER_SEARCH_FILTER)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.SEARCH_SCOPE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("1")
                .add()
                .property().name(LDAPConstants.VALIDATE_PASSWORD_POLICY)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(LDAPConstants.TRUST_EMAIL)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(LDAPConstants.USE_TRUSTSTORE_SPI)
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("always")
                .add()
                .property().name(LDAPConstants.CONNECTION_POOLING)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("true")
                .add()
                .property().name(LDAPConstants.CONNECTION_TIMEOUT)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.READ_TIMEOUT)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(LDAPConstants.PAGINATION)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("true")
                .add()
                .property().name(LDAPConstants.REFERRAL)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(KerberosConstants.ALLOW_KERBEROS_AUTHENTICATION)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(KerberosConstants.SERVER_PRINCIPAL)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(KerberosConstants.KEYTAB)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(KerberosConstants.KERBEROS_REALM)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(KerberosConstants.KERBEROS_PRINCIPAL_ATTRIBUTE)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(KerberosConstants.DEBUG)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(KerberosConstants.USE_KERBEROS_FOR_PASSWORD_AUTHENTICATION)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(LDAPConstants.CONNECTION_TRACE)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(LDAPConstants.ENABLE_LDAP_PASSWORD_POLICY)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public LDAPStorageProvider create(KeycloakSession session, ComponentModel model) {
        Map<ComponentModel, LDAPConfigDecorator> configDecorators = getLDAPConfigDecorators(session, model);

        LDAPIdentityStore ldapIdentityStore = this.ldapStoreRegistry.getLdapStore(session, model, configDecorators);
        return new LDAPStorageProvider(this, session, model, ldapIdentityStore);
    }


    // 每次请求构建 decorator 映射的开销通常可忽略
    protected Map<ComponentModel, LDAPConfigDecorator> getLDAPConfigDecorators(KeycloakSession session, ComponentModel ldapModel) {
        RealmModel realm = session.realms().getRealm(ldapModel.getParentId());
        return realm.getComponentsStream(ldapModel.getId(), LDAPStorageMapper.class.getName())
                .filter(mapperModel -> session.getKeycloakSessionFactory()
                        .getProviderFactory(LDAPStorageMapper.class, mapperModel.getProviderId()) instanceof LDAPConfigDecorator)
                .collect(Collectors.toMap(Function.identity(), mapperModel ->
                        (LDAPConfigDecorator) session.getKeycloakSessionFactory()
                                .getProviderFactory(LDAPStorageMapper.class, mapperModel.getProviderId())));
    }


    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        LDAPConfig cfg = new LDAPConfig(config.getConfig());
        UserStorageProviderModel userStorageModel = new UserStorageProviderModel(config);
        String customFilter = cfg.getCustomUserSearchFilter();
        LDAPUtils.validateCustomLdapFilter(customFilter);

        String connectionTimeout = cfg.getConnectionTimeout();
        if (connectionTimeout != null && !connectionTimeout.isEmpty()) {
            try {
                Long.parseLong(connectionTimeout);
            } catch (NumberFormatException nfe) {
                throw new ComponentValidationException("ldapErrorConnectionTimeoutNotNumber");
            }
        }

        String readTimeout = cfg.getReadTimeout();
        if (readTimeout != null && !readTimeout.isEmpty()) {
            try {
                Long.parseLong(readTimeout);
            } catch (NumberFormatException nfe) {
                throw new ComponentValidationException("ldapErrorReadTimeoutNotNumber");
            }
        }

        String connectionUrl = cfg.getConnectionUrl();
        if (connectionUrl != null) {
            if (connectionUrl.contains(",")) {
                throw new ComponentValidationException("ldapErrorConnectionUrlContainsComma");
            }
            for (String url : LDAPConstants.toLdapUrls(connectionUrl)) {
                try {
                    URI uri = URI.create(url);
                    String scheme = uri.getScheme();
                    if (scheme == null || !(scheme.equalsIgnoreCase("ldap") || scheme.equalsIgnoreCase("ldaps"))) {
                        throw new ComponentValidationException("ldapErrorInvalidConnectionUrlScheme");
                    }
                } catch (IllegalArgumentException e) {
                    throw new ComponentValidationException("ldapErrorInvalidConnectionUrl");
                }
            }
        }

        // 直接读原始配置：getConnectionPooling() 会结合 StartTLS 决定是否启用连接池
        if(cfg.isStartTls() && Boolean.parseBoolean(config.getConfig().getFirst(LDAPConstants.CONNECTION_POOLING))) {
            throw new ComponentValidationException("ldapErrorCantEnableStartTlsAndConnectionPooling");
        }

        // editMode 为必填项
        if (config.get(LDAPConstants.EDIT_MODE) == null) {
            throw new ComponentValidationException("ldapErrorEditModeMandatory");
        }

        // validatePasswordPolicy 仅 WRITABLE 模式可用
        if (cfg.getEditMode() != UserStorageProvider.EditMode.WRITABLE) {
            if (cfg.isValidatePasswordPolicy()) {
                throw new ComponentValidationException("ldapErrorValidatePasswordPolicyAvailableForWritableOnly");
            }
        }

        if (!userStorageModel.isImportEnabled() && cfg.getEditMode() == UserStorageProvider.EditMode.UNSYNCED) {
            throw new ComponentValidationException("ldapErrorCantEnableUnsyncedAndImportOff");
        }

        if (config.getId() == null) {
            // 新建组件时使用短 ID
            config.setId(KeycloakModelUtils.generateShortId());
        }
    }

    @Override
    public void init(Config.Scope config) {
        if (config.getBoolean(SECURE_REFERRAL, SECURE_REFERRAL_DEFAULT)) {
            setObjectFactoryBuilder();
        } else {
            logger.warnf("Insecure LDAP referrals are enabled. The option 'secure-referral' is deprecated and it will be removed in future releases.");
        }

        // 默认启用 plain/ssl 协议的 JNDI 连接池
        if (System.getProperty(LDAP_CONNECTION_POOL_PROTOCOL) == null) {
            System.setProperty(LDAP_CONNECTION_POOL_PROTOCOL, "plain ssl");
        }

        this.ldapStoreRegistry = new LDAPIdentityStoreRegistry();
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {

        ProviderConfigurationBuilder builder = ProviderConfigurationBuilder.create();

        builder.property()
                .name(SECURE_REFERRAL)
                .type("boolean")
                .helpText("Allow only secure LDAP referrals (deprecated)")
                .defaultValue(SECURE_REFERRAL_DEFAULT)
                .add();

        return builder.build();

    }

    @Override
    public void close() {
        this.ldapStoreRegistry = null;
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    // 按 LDAP 配置尽力创建合适的默认 mapper
    @Override
    public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
        LDAPConfig ldapConfig = new LDAPConfig(model.getConfig());

        boolean activeDirectory = ldapConfig.isActiveDirectory();
        UserStorageProvider.EditMode editMode = ldapConfig.getEditMode();
        String readOnly = String.valueOf(editMode == UserStorageProvider.EditMode.READ_ONLY || editMode == UserStorageProvider.EditMode.UNSYNCED);
        String usernameLdapAttribute = ldapConfig.getUsernameLdapAttribute();
        boolean syncRegistrations = Boolean.valueOf(model.getConfig().getFirst(LDAPConstants.SYNC_REGISTRATIONS));

        String alwaysReadValueFromLDAP = String.valueOf(editMode== UserStorageProvider.EditMode.READ_ONLY || editMode== UserStorageProvider.EditMode.WRITABLE);

        ComponentModel mapperModel;
        mapperModel = KeycloakModelUtils.createComponentModel("username", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID, LDAPStorageMapper.class.getName(),
                UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.USERNAME,
                UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, usernameLdapAttribute,
                UserAttributeLDAPStorageMapper.READ_ONLY, readOnly,
                UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, "false",
                UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
        realm.addComponentModel(mapperModel);

        // AD 部署通常以 CN 作为 RDN
        if (ldapConfig.getRdnLdapAttribute().equalsIgnoreCase(LDAPConstants.CN)) {

            if (usernameLdapAttribute.equalsIgnoreCase(LDAPConstants.CN)) {

                // AD 且用户名为 cn 时，givenName 映射到 firstName
                mapperModel = KeycloakModelUtils.createComponentModel("first name", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                        UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.FIRST_NAME,
                        UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.GIVENNAME,
                        UserAttributeLDAPStorageMapper.READ_ONLY, readOnly,
                        UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, alwaysReadValueFromLDAP,
                        UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
                realm.addComponentModel(mapperModel);

            } else {
                if (editMode == UserStorageProvider.EditMode.WRITABLE) {

                    // For AD deployments with "sAMAccountName" as username and writable, we need to map "cn" as username as well (this is needed so we can register new users from KC into LDAP) and we will map "givenName" to first name.
                    mapperModel = KeycloakModelUtils.createComponentModel("first name", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                            UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.FIRST_NAME,
                            UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.GIVENNAME,
                            UserAttributeLDAPStorageMapper.READ_ONLY, readOnly,
                            UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, alwaysReadValueFromLDAP,
                            UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
                    realm.addComponentModel(mapperModel);

                    mapperModel = KeycloakModelUtils.createComponentModel("username-cn", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                            UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.USERNAME,
                            UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.CN,
                            UserAttributeLDAPStorageMapper.READ_ONLY, readOnly,
                            UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, "false",
                            UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
                    realm.addComponentModel(mapperModel);
                } else {

                    // For read-only LDAP, we map "cn" as full name
                    mapperModel = KeycloakModelUtils.createComponentModel("full name", model.getId(), FullNameLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                            FullNameLDAPStorageMapper.LDAP_FULL_NAME_ATTRIBUTE, LDAPConstants.CN,
                            FullNameLDAPStorageMapper.READ_ONLY, readOnly,
                            FullNameLDAPStorageMapper.WRITE_ONLY, "false");
                    realm.addComponentModel(mapperModel);
                }
            }
        } else {
            mapperModel = KeycloakModelUtils.createComponentModel("first name", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                    UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.FIRST_NAME,
                    UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.CN,
                    UserAttributeLDAPStorageMapper.READ_ONLY, readOnly,
                    UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, alwaysReadValueFromLDAP,
                    UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
            realm.addComponentModel(mapperModel);
        }

        mapperModel = KeycloakModelUtils.createComponentModel("last name", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.LAST_NAME,
                UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.SN,
                UserAttributeLDAPStorageMapper.READ_ONLY, readOnly,
                UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, alwaysReadValueFromLDAP,
                UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
        realm.addComponentModel(mapperModel);

        mapperModel = KeycloakModelUtils.createComponentModel("email", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.EMAIL,
                UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.EMAIL,
                UserAttributeLDAPStorageMapper.READ_ONLY, readOnly,
                UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, "false",
                UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "false");
        realm.addComponentModel(mapperModel);

        String createTimestampLdapAttrName = activeDirectory ? "whenCreated" : LDAPConstants.CREATE_TIMESTAMP;
        String modifyTimestampLdapAttrName = activeDirectory ? "whenChanged" : LDAPConstants.MODIFY_TIMESTAMP;

        // 创建时间戳只读映射
        mapperModel = KeycloakModelUtils.createComponentModel("creation date", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, LDAPConstants.CREATE_TIMESTAMP,
                UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, createTimestampLdapAttrName,
                UserAttributeLDAPStorageMapper.READ_ONLY, "true",
                UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, alwaysReadValueFromLDAP,
                UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "false");
        realm.addComponentModel(mapperModel);

        // map modifyTimeStamp as read-only
        mapperModel = KeycloakModelUtils.createComponentModel("modify date", model.getId(), UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, LDAPConstants.MODIFY_TIMESTAMP,
                UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, modifyTimestampLdapAttrName,
                UserAttributeLDAPStorageMapper.READ_ONLY, "true",
                UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, alwaysReadValueFromLDAP,
                UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "false");
        realm.addComponentModel(mapperModel);

        // MSAD 账户控制状态同步 mapper
        if (activeDirectory) {
            mapperModel = KeycloakModelUtils.createComponentModel("MSAD account controls", model.getId(), MSADUserAccountControlStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                    MSADUserAccountControlStorageMapper.ALWAYS_READ_ENABLED_VALUE_FROM_LDAP, alwaysReadValueFromLDAP);
            realm.addComponentModel(mapperModel);
        }

        LDAPProviderKerberosConfig kerberosConfig = new LDAPProviderKerberosConfig(model);
        if (kerberosConfig.isAllowKerberosAuthentication()) {
            CredentialHelper.setOrReplaceAuthenticationRequirement(session, realm, CredentialRepresentation.KERBEROS,
                    AuthenticationExecutionModel.Requirement.ALTERNATIVE, AuthenticationExecutionModel.Requirement.DISABLED);
        }

        if (kerberosConfig.getKerberosPrincipalAttribute() == null) {
            String defaultKerberosUserPrincipalAttr = LDAPUtils.getDefaultKerberosUserPrincipalAttribute(ldapConfig.getVendor());
            model.getConfig().putSingle(KerberosConstants.KERBEROS_PRINCIPAL_ATTRIBUTE, defaultKerberosUserPrincipalAttr);
            realm.updateComponent(model);
        }

        if (kerberosConfig.getKerberosPrincipalAttribute() != null) {
            mapperModel = KeycloakModelUtils.createComponentModel("Kerberos principal attribute mapper", model.getId(), KerberosPrincipalAttributeMapperFactory.PROVIDER_ID, LDAPStorageMapper.class.getName());
            realm.addComponentModel(mapperModel);
        }

        // 开启同步注册且使用 Password Modify 扩展时，写入随机 userPassword 以便用户可登录
        // random "userPassword" every time when creating user. Otherwise users won't be able to register and login
        if (!activeDirectory && syncRegistrations && ldapConfig.useExtendedPasswordModifyOp()) {
            mapperModel = KeycloakModelUtils.createComponentModel("random initial password", model.getId(), HardcodedLDAPAttributeMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                    HardcodedLDAPAttributeMapper.LDAP_ATTRIBUTE_NAME, LDAPConstants.USER_PASSWORD_ATTRIBUTE,
                    HardcodedLDAPAttributeMapper.LDAP_ATTRIBUTE_VALUE, HardcodedLDAPAttributeMapper.RANDOM_ATTRIBUTE_VALUE);
            realm.addComponentModel(mapperModel);
        }
    }

    @Override
    public void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        boolean allowKerberosCfgOld = Boolean.valueOf(oldModel.getConfig().getFirst(KerberosConstants.ALLOW_KERBEROS_AUTHENTICATION));
        boolean allowKerberosCfgNew = Boolean.valueOf(newModel.getConfig().getFirst(KerberosConstants.ALLOW_KERBEROS_AUTHENTICATION));
        if (!allowKerberosCfgOld && allowKerberosCfgNew) {
            CredentialHelper.setOrReplaceAuthenticationRequirement(session, realm, CredentialRepresentation.KERBEROS,
                    AuthenticationExecutionModel.Requirement.ALTERNATIVE, AuthenticationExecutionModel.Requirement.DISABLED);
        } else if(allowKerberosCfgOld && !allowKerberosCfgNew) {
            CredentialHelper.setOrReplaceAuthenticationRequirement(session, realm, CredentialRepresentation.KERBEROS,
                    AuthenticationExecutionModel.Requirement.DISABLED, AuthenticationExecutionModel.Requirement.ALTERNATIVE);
        } // else: keep current settings

        LDAPConfig oldConfig = new LDAPConfig(oldModel.getConfig());
        LDAPConfig newConfig = new LDAPConfig(newModel.getConfig());
        if (!oldConfig.getUsernameLdapAttribute().equals(newConfig.getUsernameLdapAttribute())) {
            // 用户名 LDAP 属性变更时同步到 username mapper
            ComponentModel usernameMapperModel = realm.getComponentsStream(oldModel.getId(), LDAPStorageMapper.class.getName())
                    .filter(mapper -> "username".equals(mapper.getName()))
                    .findFirst().orElse(null);
            if (usernameMapperModel != null) {
                usernameMapperModel.getConfig().putSingle(UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, newConfig.getUsernameLdapAttribute());
                realm.updateComponent(usernameMapperModel);
            }
        }
    }

    @Override
    public void preRemove(KeycloakSession session, RealmModel realm, ComponentModel model) {
        String allowKerberosCfg = model.getConfig().getFirst(KerberosConstants.ALLOW_KERBEROS_AUTHENTICATION);
        if (Boolean.valueOf(allowKerberosCfg)) {
            CredentialHelper.setOrReplaceAuthenticationRequirement(session, realm, CredentialRepresentation.KERBEROS,
                    AuthenticationExecutionModel.Requirement.DISABLED, null);
        }
    }

    @Override
    public SynchronizationResult sync(KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        syncMappers(sessionFactory, realmId, model);

        logger.infof("Sync all users from LDAP to local store: realm: %s, federation provider: %s", realmId, model.getName());

        try (LDAPQuery userQuery = createQuery(sessionFactory, realmId, model)) {
            SynchronizationResult syncResult = syncImpl(sessionFactory, userQuery, realmId, model);

            // TODO: 清理 federationLink 存在但 LDAP 中已不存在的本地用户

            logger.infof("Sync all users finished: %s", syncResult.getStatus());
            return syncResult;
        }
    }

    @Override
    public SynchronizationResult syncSince(Date lastSync, KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        syncMappers(sessionFactory, realmId, model);

        logger.infof("Sync changed users from LDAP to local store: realm: %s, federation provider: %s, last sync time: " + lastSync, realmId, model.getName());

        // 同步自 lastSync 以来新建或修改的用户
        LDAPQueryConditionsBuilder conditionsBuilder = new LDAPQueryConditionsBuilder();
        Condition createCondition = conditionsBuilder.greaterThanOrEqualTo(LDAPConstants.CREATE_TIMESTAMP, lastSync);
        Condition modifyCondition = conditionsBuilder.greaterThanOrEqualTo(LDAPConstants.MODIFY_TIMESTAMP, lastSync);
        Condition orCondition = conditionsBuilder.orCondition(createCondition, modifyCondition);

        try (LDAPQuery userQuery = createQuery(sessionFactory, realmId, model)) {
            userQuery.addWhereCondition(orCondition);
            SynchronizationResult result = syncImpl(sessionFactory, userQuery, realmId, model);

            logger.infof("Sync changed users finished: %s", result.getStatus());
            return result;
        }
    }

    protected void syncMappers(KeycloakSessionFactory sessionFactory, final String realmId, final ComponentModel model) {
        KeycloakModelUtils.runJobInTransaction(sessionFactory, new KeycloakSessionTask() {

            @Override
            public void run(KeycloakSession session) {
                RealmModel realm = session.realms().getRealm(realmId);
                session.getContext().setRealm(realm);
                session.getProvider(UserStorageProvider.class, model);
                realm.getComponentsStream(model.getId(), LDAPStorageMapper.class.getName())
                        .forEach(mapperModel -> {
                            SynchronizationResult syncResult = session.getProvider(LDAPStorageMapper.class, mapperModel)
                                    .syncDataFromFederationProviderToKeycloak(realm);
                            if (syncResult.getAdded() > 0 || syncResult.getUpdated() > 0 || syncResult.getRemoved() > 0
                                    || syncResult.getFailed() > 0) {
                                logger.infof("Sync of federation mapper '%s' finished. Status: %s", mapperModel.getName(), syncResult.toString());
                            }
                        });
            }

        });
    }

    protected SynchronizationResult syncImpl(KeycloakSessionFactory sessionFactory, LDAPQuery userQuery, final String realmId, final ComponentModel fedModel) {

        final SynchronizationResult syncResult = new SynchronizationResult();

        LDAPConfig ldapConfig = new LDAPConfig(fedModel.getConfig());
        boolean pagination = ldapConfig.isPagination();
        if (pagination) {
            int pageSize = ldapConfig.getBatchSizeForSync();

            boolean nextPage = true;
            while (nextPage) {
                userQuery.setLimit(pageSize);
                final List<LDAPObject> users = userQuery.getResultList();
                nextPage = userQuery.getPaginationContext().hasNextPage();
                SynchronizationResult currentPageSync = importLdapUsers(sessionFactory, realmId, fedModel, users);
                syncResult.add(currentPageSync);
            }
        } else {
            // 无 LDAP 分页时一次性拉取全部结果
            final List<LDAPObject> users = userQuery.getResultList();
            SynchronizationResult currentSync = importLdapUsers(sessionFactory, realmId, fedModel, users);
            syncResult.add(currentSync);
        }

        return syncResult;
    }

    /**
     * 必须在 try-with-resources 中调用，否则 Vault 密钥可能泄漏。
     *  !! This function must be called from try-with-resources block, otherwise Vault secrets may be leaked !!
     * @param sessionFactory
     * @param realmId
     * @param model
     * @return
     */
    private LDAPQuery createQuery(KeycloakSessionFactory sessionFactory, final String realmId, final ComponentModel model) {
        class QueryHolder {
            LDAPQuery query;
        }

        final QueryHolder queryHolder = new QueryHolder();
        KeycloakModelUtils.runJobInTransaction(sessionFactory, new KeycloakSessionTask() {

            @Override
            public void run(KeycloakSession session) {
                session.getContext().setRealm(session.realms().getRealm(realmId));

                LDAPStorageProvider ldapFedProvider = (LDAPStorageProvider)session.getProvider(UserStorageProvider.class, model);
                RealmModel realm = session.realms().getRealm(realmId);
                queryHolder.query = LDAPUtils.createQueryForUserSearch(ldapFedProvider, realm);
            }

        });
        return queryHolder.query;
    }

    protected SynchronizationResult importLdapUsers(KeycloakSessionFactory sessionFactory, final String realmId, final ComponentModel fedModel, List<LDAPObject> ldapUsers) {
        final SynchronizationResult syncResult = new SynchronizationResult();

        class BooleanHolder {
            private boolean value = true;
        }
        final BooleanHolder exists = new BooleanHolder();

        for (final LDAPObject ldapUser : ldapUsers) {

            try {

                // 每个用户独立事务，避免单条失败导致整批回滚
                KeycloakModelUtils.runJobInTransaction(sessionFactory, new KeycloakSessionTask() {

                    @Override
                    public void run(KeycloakSession session) {
                        LDAPStorageProvider ldapFedProvider = (LDAPStorageProvider)session.getProvider(UserStorageProvider.class, fedModel);
                        RealmModel currentRealm = session.realms().getRealm(realmId);
                        session.getContext().setRealm(currentRealm);

                        String username = LDAPUtils.getUsername(ldapUser, ldapFedProvider.getLdapIdentityStore().getConfig());
                        exists.value = true;
                        LDAPUtils.checkUuid(ldapUser, ldapFedProvider.getLdapIdentityStore().getConfig());
                        UserModel currentUserLocal = UserStoragePrivateUtil.userLocalStorage(session).getUserByUsername(currentRealm, username);
                        Optional<UserModel> userModelOptional = UserStoragePrivateUtil.userLocalStorage(session)
                                .searchForUserByUserAttributeStream(currentRealm, LDAPConstants.LDAP_ID, ldapUser.getUuid())
                                .findFirst();
                        if (!userModelOptional.isPresent() && currentUserLocal == null) {
                            // 新用户写入 Keycloak 本地库
                            exists.value = false;
                            ldapFedProvider.importUserFromLDAP(session, currentRealm, ldapUser);
                            syncResult.increaseAdded();

                        } else {
                            UserModel currentUser = userModelOptional.isPresent() ? userModelOptional.get() : currentUserLocal;
                            if ((fedModel.getId().equals(currentUser.getFederationLink())) && (ldapUser.getUuid().equals(currentUser.getFirstAttribute(LDAPConstants.LDAP_ID)))) {

                                // 更新已有 Keycloak 用户属性
                                LDAPMappersComparator ldapMappersComparator = new LDAPMappersComparator(ldapFedProvider.getLdapIdentityStore().getConfig());
                                currentRealm.getComponentsStream(fedModel.getId(), LDAPStorageMapper.class.getName())
                                        .sorted(ldapMappersComparator.sortDesc())
                                        .forEachOrdered(mapperModel -> {
                                            LDAPStorageMapper ldapMapper = ldapFedProvider.getMapperManager().getMapper(mapperModel);
                                            ldapMapper.onImportUserFromLDAP(ldapUser, currentUser, currentRealm, false);
                                        });

                                UserCache userCache = UserStorageUtil.userCache(session);
                                if (userCache != null) {
                                    userCache.evict(currentRealm, currentUser);
                                }
                                logger.debugf("Updated user from LDAP: %s", currentUser.getUsername());
                                syncResult.increaseUpdated();
                            } else {
                                logger.warnf("User with ID '%s' is not updated during sync as he already exists in Keycloak database but is not linked to federation provider '%s'", ldapUser.getUuid(), fedModel.getName());
                                syncResult.increaseFailed();
                            }
                        }
                    }

                });
            } catch (ModelException me) {
                logger.error("Failed during import user from LDAP", me);
                syncResult.increaseFailed();

                // 本事务中已添加的用户在失败时回滚删除
                if (!exists.value) {
                    KeycloakModelUtils.runJobInTransaction(sessionFactory, new KeycloakSessionTask() {

                        @Override
                        public void run(KeycloakSession session) {
                            LDAPStorageProvider ldapFedProvider = (LDAPStorageProvider)session.getProvider(UserStorageProvider.class, fedModel);
                            RealmModel currentRealm = session.realms().getRealm(realmId);
                            session.getContext().setRealm(currentRealm);

                            String username = null;
                            try {
                                username = LDAPUtils.getUsername(ldapUser, ldapFedProvider.getLdapIdentityStore().getConfig());
                            } catch (ModelException ignore) {
                            }

                            if (username != null) {
                                UserModel existing = UserStoragePrivateUtil.userLocalStorage(session).getUserByUsername(currentRealm, username);
                                if (existing != null) {
                                    UserCache userCache = UserStorageUtil.userCache(session);
                                    if (userCache != null) {
                                        userCache.evict(currentRealm, existing);
                                    }
                                    UserStoragePrivateUtil.userLocalStorage(session).removeUser(currentRealm, existing);
                                }
                            }
                        }

                    });
                }
            }
        }

        return syncResult;
    }

    protected SPNEGOAuthenticator createSPNEGOAuthenticator(String spnegoToken, CommonKerberosConfig kerberosConfig) {
        KerberosServerSubjectAuthenticator kerberosAuth = createKerberosSubjectAuthenticator(kerberosConfig);
        return new SPNEGOAuthenticator(kerberosConfig, kerberosAuth, spnegoToken);
    }

    protected KerberosServerSubjectAuthenticator createKerberosSubjectAuthenticator(CommonKerberosConfig kerberosConfig) {
        return new KerberosServerSubjectAuthenticator(kerberosConfig);
    }

    protected KerberosUsernamePasswordAuthenticator createKerberosUsernamePasswordAuthenticator(CommonKerberosConfig kerberosConfig) {
        return new KerberosUsernamePasswordAuthenticator(kerberosConfig);
    }

    private void setObjectFactoryBuilder() {
        try {
            NamingManager.setObjectFactoryBuilder(new ObjectFactoryBuilder());
        } catch (NamingException | IllegalStateException e) {
            if (e instanceof IllegalStateException && ObjectFactoryBuilder.isSet()) {
                return;
            }

            throw new RuntimeException("Failed to set the server JNDI ObjectFactoryBuilder", e);
        }
    }
 }
