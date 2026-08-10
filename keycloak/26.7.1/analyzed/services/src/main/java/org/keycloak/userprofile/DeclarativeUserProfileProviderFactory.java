/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.userprofile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.requiredactions.TermsAndConditions;
import org.keycloak.authentication.requiredactions.UpdateEmail;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.component.AmphibianProviderFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.validator.OrganizationMemberValidator;
import org.keycloak.protocol.oid4vc.userprofile.DuplicateDidValidator;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPAttributePermissions;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.services.messages.Messages;
import org.keycloak.userprofile.config.UPConfigUtils;
import org.keycloak.userprofile.validator.BlankAttributeValidator;
import org.keycloak.userprofile.validator.BrokeringFederatedUsernameHasValueValidator;
import org.keycloak.userprofile.validator.DuplicateEmailValidator;
import org.keycloak.userprofile.validator.DuplicateUsernameValidator;
import org.keycloak.userprofile.validator.EmailExistsAsUsernameValidator;
import org.keycloak.userprofile.validator.ReadOnlyAttributeUnchangedValidator;
import org.keycloak.userprofile.validator.RegistrationEmailAsUsernameEmailValueValidator;
import org.keycloak.userprofile.validator.RegistrationEmailAsUsernameUsernameValueValidator;
import org.keycloak.userprofile.validator.RegistrationUsernameExistsValidator;
import org.keycloak.userprofile.validator.UsernameHasValueValidator;
import org.keycloak.userprofile.validator.UsernameMutationValidator;
import org.keycloak.utils.StringUtil;
import org.keycloak.validate.ValidatorConfig;
import org.keycloak.validate.validators.EmailValidator;
import org.keycloak.validate.validators.PatternValidator;

import org.jspecify.annotations.NonNull;

import static java.util.Optional.ofNullable;

import static org.keycloak.common.util.ObjectUtil.isBlank;
import static org.keycloak.userprofile.DefaultAttributes.READ_ONLY_ATTRIBUTE_KEY;
import static org.keycloak.userprofile.UserProfileContext.ACCOUNT;
import static org.keycloak.userprofile.UserProfileContext.IDP_REVIEW;
import static org.keycloak.userprofile.UserProfileContext.REGISTRATION;
import static org.keycloak.userprofile.UserProfileContext.SCIM;
import static org.keycloak.userprofile.UserProfileContext.UPDATE_EMAIL;
import static org.keycloak.userprofile.UserProfileContext.UPDATE_PROFILE;
import static org.keycloak.userprofile.UserProfileContext.USER_API;

/**
 * 声明式用户 Profile 提供者工厂。
 * <p>在初始化时为各 {@link UserProfileContext} 注册元数据、内置校验器与只读属性规则，并解析领域级 {@link UPConfig} 配置。</p>
 */
public class DeclarativeUserProfileProviderFactory implements UserProfileProviderFactory, AmphibianProviderFactory<UserProfileProvider> {

    /** 管理员不可编辑属性的正则表达式配置键。 */
    public static final String CONFIG_ADMIN_READ_ONLY_ATTRIBUTES = "admin-read-only-attributes";
    /** 终端用户不可编辑属性的正则表达式配置键。 */
    public static final String CONFIG_READ_ONLY_ATTRIBUTES = "read-only-attributes";
    /** 邮箱本地部分最大长度配置键。 */
    public static final String MAX_EMAIL_LOCAL_PART_LENGTH = "max-email-local-part-length";

    /** 提供者 SPI 标识符。 */
    public static final String ID = "declarative-user-profile";
    /** 提供者优先级（数值越小越优先）。 */
    public static final int PROVIDER_PRIORITY = 1;

    /**
     * 内置只读属性名列表，用于创建只读校验规则。
     * <p>与用户 Profile 的使用场景无关，覆盖 LDAP/Kerberos 等内部属性。</p>
     */
    private static final String[] DEFAULT_READ_ONLY_ATTRIBUTES = { "KERBEROS_PRINCIPAL", "LDAP_ID", "LDAP_ENTRY_DN", "CREATED_TIMESTAMP", "createTimestamp", "modifyTimestamp", "userCertificate", "saml.persistent.name.id.for.*", "ENABLED", "EMAIL_VERIFIED", "disabledReason", UserModel.EMAIL_PENDING };
    private static final String[] DEFAULT_ADMIN_READ_ONLY_ATTRIBUTES = { "KERBEROS_PRINCIPAL", "LDAP_ID", "LDAP_ENTRY_DN", "CREATED_TIMESTAMP", "createTimestamp", "modifyTimestamp" };
    private static final Pattern readOnlyAttributesPattern = getRegexPatternString(DEFAULT_READ_ONLY_ATTRIBUTES);
    private static final Pattern adminReadOnlyAttributesPattern = getRegexPatternString(DEFAULT_ADMIN_READ_ONLY_ATTRIBUTES);
    private static final String ANNOTATION_SCIM_SCHEMA_ATTRIBUTE = "kc.scim.schema.attribute";

    /** 系统默认 UPConfig（Quarkus 构建时可预解析）。 */
    private static volatile UPConfig PARSED_DEFAULT_RAW_CONFIG;
    /** 各上下文对应的 Profile 元数据注册表。 */
    private final Map<UserProfileContext, UserProfileMetadata> contextualMetadataRegistry = new HashMap<>();
    /** 当前工厂解析后的用户 Profile 配置。 */
    private UPConfig parsedConfig;

    /** 设置系统默认配置（仅首次生效）。 */
    public static void setDefaultConfig(UPConfig defaultConfig) {
        if (PARSED_DEFAULT_RAW_CONFIG == null) {
            PARSED_DEFAULT_RAW_CONFIG = defaultConfig;
        }
    }

    /** 判断当前上下文下用户名是否可编辑。 */
    private static boolean editUsernameCondition(AttributeContext c) {
        KeycloakSession session = c.getSession();
        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();

        if (REGISTRATION.equals(c.getContext())
                || IDP_REVIEW.equals(c.getContext())
                || isNewUser(c)) {
            return !realm.isRegistrationEmailAsUsername();
        }

        if (realm.isRegistrationEmailAsUsername()) {
            return false;
        }

        return realm.isEditUsernameAllowed();
    }

    /** 判断当前上下文下用户名是否可见。 */
    private static boolean readUsernameCondition(AttributeContext c) {
        KeycloakSession session = c.getSession();
        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();

        return switch (c.getContext()) {
            case REGISTRATION, IDP_REVIEW -> !realm.isRegistrationEmailAsUsername();
            case UPDATE_PROFILE -> {
                if (realm.isRegistrationEmailAsUsername()) {
                    yield false;
                }
                yield realm.isEditUsernameAllowed();
            }
            case UPDATE_EMAIL -> false;
            default -> true;
        };

    }

    /** 判断当前上下文下邮箱是否可编辑。 */
    private static boolean editEmailCondition(AttributeContext c) {
        RealmModel realm = c.getSession().getContext().getRealm();

        if (REGISTRATION.equals(c.getContext()) || USER_API.equals(c.getContext())) {
            return true;
        }

        if (UpdateEmail.isEnabled(realm)) {
            if (UPDATE_PROFILE.equals(c.getContext())) {
                UserModel user = c.getUser();

                if (!isNewUser(c)) {
                    if (StringUtil.isBlank(user.getEmail())) {
                        // 用户尚无邮箱时，允许通过 UPDATE_PROFILE 设置
                        return true;
                    }

                    List<String> values = c.getAttribute().getValue();

                    if (values == null || values.isEmpty()) {
                        // 用户已有邮箱时忽略空值，应通过更新邮箱流程修改
                        return false;
                    }
                }
            }

            return !(UPDATE_PROFILE.equals(c.getContext()) || ACCOUNT.equals(c.getContext()));
        }

        return isNewUser(c) || !realm.isRegistrationEmailAsUsername() || realm.isEditUsernameAllowed();
    }

    /** 判断当前上下文下邮箱是否可见。 */
    private static boolean readEmailCondition(AttributeContext c) {
        UserProfileContext context = c.getContext();

        if (REGISTRATION.equals(context) || USER_API.equals(c.getContext())) {
            return true;
        }

        KeycloakSession session = c.getSession();

        if (UpdateEmail.isEnabled(session.getContext().getRealm())) {
            if (UPDATE_PROFILE.equals(c.getContext())) {
                List<String> value = c.getAttribute().getValue();

                if (value.isEmpty() && !c.getMetadata().isReadOnly(c)) {
                    // 邮箱未设置且非只读时，在 UPDATE_PROFILE 页面展示邮箱字段
                    return true;
                }
            }

            return !UPDATE_PROFILE.equals(context);
        }

        if (UPDATE_PROFILE.equals(context)) {
            RealmModel realm = session.getContext().getRealm();

            if (realm.isRegistrationEmailAsUsername()) {
                return realm.isEditUsernameAllowed();
            }
        }

        return true;
    }

    /** 领域是否启用国际化（locale 字段可见性）。 */
    private static boolean isInternationalizationEnabled(AttributeContext context) {
        RealmModel realm = context.getSession().getContext().getRealm();
        return realm.isInternationalizationEnabled();
    }

    /** 领域是否启用条款与条件必需操作。 */
    private static boolean isTermAndConditionsEnabled(AttributeContext context) {
        RealmModel realm = context.getSession().getContext().getRealm();
        RequiredActionProviderModel tacModel = realm.getRequiredActionProviderByAlias(
                UserModel.RequiredAction.TERMS_AND_CONDITIONS.name());
        return tacModel != null && tacModel.isEnabled();
    }

    /** 是否为新建用户（尚无 UserModel）。 */
    private static boolean isNewUser(AttributeContext c) {
        return c.getUser() == null;
    }

    /** 将只读属性名数组编译为不区分大小写的正则 Pattern。 */
    public static Pattern getRegexPatternString(String[] builtinReadOnlyAttributes) {
        if (builtinReadOnlyAttributes != null) {
            List<String> readOnlyAttributes = new ArrayList<>(Arrays.asList(builtinReadOnlyAttributes));

            String regexStr = readOnlyAttributes.stream()
                    .map(configAttrName -> configAttrName.endsWith("*")
                            ? "^" + Pattern.quote(configAttrName.substring(0, configAttrName.length() - 1)) + ".*$"
                            : "^" + Pattern.quote(configAttrName) + "$")
                    .collect(Collectors.joining("|"));
            regexStr = "(?i:" + regexStr + ")";

            return Pattern.compile(regexStr);
        }

        return null;
    }

    @Override
    public void init(Config.Scope config) {
        initDefaultConfiguration(config);

        // 重新部署时清空上下文元数据注册表
        contextualMetadataRegistry.clear();
        Pattern pattern = getRegexPatternString(config.getArray(CONFIG_READ_ONLY_ATTRIBUTES));
        AttributeValidatorMetadata readOnlyValidator = null;

        if (pattern != null) {
            readOnlyValidator = createReadOnlyAttributeUnchangedValidator(pattern);
        }

        addContextualProfileMetadata(configureUserProfile(createBrokeringProfile(readOnlyValidator)));
        addContextualProfileMetadata(configureUserProfile(createAccountProfile(readOnlyValidator)));
        addContextualProfileMetadata(configureUserProfile(createDefaultProfile(UPDATE_PROFILE, readOnlyValidator)));
        if (Profile.isFeatureEnabled(Profile.Feature.UPDATE_EMAIL)) {
            addContextualProfileMetadata(configureUserProfile(createDefaultProfile(UPDATE_EMAIL, readOnlyValidator)));
        }
        addContextualProfileMetadata(configureUserProfile(createRegistrationUserCreationProfile(readOnlyValidator)));
        addContextualProfileMetadata(configureUserProfile(createUserResourceValidation(config)));
        addContextualProfileMetadata(configureUserProfile(createScimProfile(readOnlyValidator)));
    }

    /** 构建 SCIM 上下文 Profile，附加 SCIM schema 注解。 */
    private @NonNull UserProfileMetadata createScimProfile(AttributeValidatorMetadata readOnlyValidator) {
        UserProfileMetadata metadata = createDefaultProfile(SCIM, readOnlyValidator);
        String coreSchema = "urn:ietf:params:scim:schemas:core:2.0:User";

        metadata.getAttribute(UserModel.USERNAME).get(0)
                .addAnnotations(Map.of(ANNOTATION_SCIM_SCHEMA_ATTRIBUTE, "userName"));
        metadata.getAttribute(UserModel.EMAIL).get(0)
                .addAnnotations(Map.of(ANNOTATION_SCIM_SCHEMA_ATTRIBUTE, "emails"));
        metadata.addAttribute(UserModel.FIRST_NAME, -1, AttributeMetadata.ALWAYS_TRUE, AttributeMetadata.ALWAYS_TRUE)
                .addAnnotations(Map.of(ANNOTATION_SCIM_SCHEMA_ATTRIBUTE, "name.givenName"));
        metadata.addAttribute(UserModel.LAST_NAME, -1, AttributeMetadata.ALWAYS_TRUE, AttributeMetadata.ALWAYS_TRUE)
                .addAnnotations(Map.of(ANNOTATION_SCIM_SCHEMA_ATTRIBUTE, "name.familyName"));
        metadata.addAttribute(UserModel.ENABLED, -1, AttributeMetadata.ALWAYS_TRUE, AttributeMetadata.ALWAYS_TRUE)
                .addAnnotations(Map.of(ANNOTATION_SCIM_SCHEMA_ATTRIBUTE, "active"));
        metadata.addAttribute(UserModel.CREATED_TIMESTAMP, -1, AttributeMetadata.ALWAYS_FALSE, AttributeMetadata.ALWAYS_TRUE)
                .setRequired(AttributeMetadata.ALWAYS_FALSE)
                .addAnnotations(Map.of(ANNOTATION_SCIM_SCHEMA_ATTRIBUTE, "meta.created"));
        metadata.addAttribute(UserModel.LOCALE, -1, DeclarativeUserProfileProviderFactory::isInternationalizationEnabled, DeclarativeUserProfileProviderFactory::isInternationalizationEnabled)
                .setRequired(AttributeMetadata.ALWAYS_FALSE)
                .addAnnotations(Map.of(ANNOTATION_SCIM_SCHEMA_ATTRIBUTE, "locale"))
                .setSelector(c -> {
                    RealmModel realm = c.getSession().getContext().getRealm();
                    return realm.isInternationalizationEnabled();
                });

        return metadata;
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(CONFIG_READ_ONLY_ATTRIBUTES)
                .type(ProviderConfigProperty.MULTIVALUED_STRING_TYPE)
                .helpText("Array of regular expressions to identify fields that should be treated read-only so users can't change them.")
                .add()

                .property()
                .name(CONFIG_ADMIN_READ_ONLY_ATTRIBUTES)
                .type(ProviderConfigProperty.MULTIVALUED_STRING_TYPE)
                .helpText("Array of regular expressions to identify fields that should be treated read-only so administrators can't change them.")
                .add()

                .property()
                .name(MAX_EMAIL_LOCAL_PART_LENGTH)
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("To set user profile max email local part length")
                .add()

                .build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property().name(DeclarativeUserProfileProvider.UP_COMPONENT_CONFIG_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .build();
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        String upConfigJson = model == null ? null : model.get(DeclarativeUserProfileProvider.UP_COMPONENT_CONFIG_KEY);

        if (!isBlank(upConfigJson)) {
            try {
                UPConfig upc = UPConfigUtils.parseConfig(upConfigJson);
                List<String> errors = UPConfigUtils.validate(session, upc);

                if (!errors.isEmpty()) {
                    throw new ComponentValidationException(errors.toString());
                }
            } catch (IOException e) {
                throw new ComponentValidationException(e.getMessage(), e);
            }
        }

        // 清除组件缓存，下次使用时重新解析并应用配置
        // 见 configureUserProfile(metadata, session)
        if (model != null) {
            model.removeNote(DeclarativeUserProfileProvider.PARSED_CONFIG_COMPONENT_KEY);
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int order() {
        return PROVIDER_PRIORITY;
    }

    @Override
    public String getHelpText() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public DeclarativeUserProfileProvider create(KeycloakSession session) {
        return new DeclarativeUserProfileProvider(session, this);
    }

    /**
     * 在 init 阶段用默认领域配置装饰上下文 Profile 元数据。
     *
     * @param metadata 待装饰的 Profile 元数据
     * @return 装饰后的元数据
     */
    protected UserProfileMetadata configureUserProfile(UserProfileMetadata metadata) {
        // 各上下文的默认元数据基于当前解析的领域配置
        return new DeclarativeUserProfileProvider(null, this).decorateUserProfileForCache(metadata, parsedConfig);
    }

    /** 创建只读属性不可变校验器元数据。 */
    private AttributeValidatorMetadata createReadOnlyAttributeUnchangedValidator(Pattern pattern) {
        return new AttributeValidatorMetadata(ReadOnlyAttributeUnchangedValidator.ID,
                ValidatorConfig.builder().config(ReadOnlyAttributeUnchangedValidator.CFG_PATTERN, pattern)
                        .build());
    }

    /** 注册上下文元数据；组织特性下为 email 附加成员校验器。 */
    private void addContextualProfileMetadata(UserProfileMetadata metadata) {
        if (contextualMetadataRegistry.putIfAbsent(metadata.getContext(), metadata) != null) {
            throw new IllegalStateException("Multiple profile metadata found for context " + metadata.getContext());
        }

        if (Profile.isFeatureEnabled(Feature.ORGANIZATION)) {
            for (AttributeMetadata attribute : metadata.getAttributes()) {
                String name = attribute.getName();

                if (UserModel.EMAIL.equals(name)) {
                    attribute.addValidators(List.of(new AttributeValidatorMetadata(OrganizationMemberValidator.ID)));
                }
            }
        }
    }

    /** 构建 IdP 联邦审核（IDP_REVIEW）上下文 Profile。 */
    private UserProfileMetadata createBrokeringProfile(AttributeValidatorMetadata readOnlyValidator) {
        UserProfileMetadata metadata = new UserProfileMetadata(IDP_REVIEW);

        metadata.addAttribute(UserModel.USERNAME, -2, DeclarativeUserProfileProviderFactory::editUsernameCondition,
                DeclarativeUserProfileProviderFactory::readUsernameCondition, new AttributeValidatorMetadata(BrokeringFederatedUsernameHasValueValidator.ID)).setAttributeDisplayName("${username}");

        metadata.addAttribute(UserModel.EMAIL, -1,
                        new AttributeValidatorMetadata(BlankAttributeValidator.ID, BlankAttributeValidator.createConfig(Messages.MISSING_EMAIL, true)))
                .setAttributeDisplayName("${email}");

        List<AttributeValidatorMetadata> readonlyValidators = new ArrayList<>();

        readonlyValidators.add(createReadOnlyAttributeUnchangedValidator(readOnlyAttributesPattern));

        if (readOnlyValidator != null) {
            readonlyValidators.add(readOnlyValidator);
        }

        metadata.addAttribute(READ_ONLY_ATTRIBUTE_KEY, 1000, readonlyValidators);

        return metadata;
    }

    /** 构建用户注册创建上下文 Profile。 */
    private UserProfileMetadata createRegistrationUserCreationProfile(AttributeValidatorMetadata readOnlyValidator) {
        UserProfileMetadata metadata = createDefaultProfile(REGISTRATION, readOnlyValidator);

        metadata.getAttribute(UserModel.USERNAME).get(0).addValidators(Arrays.asList(
                new AttributeValidatorMetadata(RegistrationEmailAsUsernameUsernameValueValidator.ID), new AttributeValidatorMetadata(RegistrationUsernameExistsValidator.ID), new AttributeValidatorMetadata(UsernameHasValueValidator.ID)));

        metadata.getAttribute(UserModel.EMAIL).get(0).addValidators(Collections.singletonList(
                new AttributeValidatorMetadata(RegistrationEmailAsUsernameEmailValueValidator.ID)));

        metadata.addAttribute(UserModel.LOCALE, -1, DeclarativeUserProfileProviderFactory::isInternationalizationEnabled, DeclarativeUserProfileProviderFactory::isInternationalizationEnabled)
                .setRequired(AttributeMetadata.ALWAYS_FALSE);

        addAttributeUserDid(metadata);

        return metadata;
    }

    /** 构建指定上下文的默认 Profile（用户名/邮箱/只读键）。 */
    private UserProfileMetadata createDefaultProfile(UserProfileContext context, AttributeValidatorMetadata readOnlyValidator) {
        UserProfileMetadata metadata = new UserProfileMetadata(context);

        metadata.addAttribute(UserModel.USERNAME, -2,
                DeclarativeUserProfileProviderFactory::editUsernameCondition,
                DeclarativeUserProfileProviderFactory::readUsernameCondition,
                new AttributeValidatorMetadata(UsernameHasValueValidator.ID),
                new AttributeValidatorMetadata(DuplicateUsernameValidator.ID),
                new AttributeValidatorMetadata(UsernameMutationValidator.ID)).setAttributeDisplayName("${username}");

        metadata.addAttribute(UserModel.EMAIL, -1,
                        DeclarativeUserProfileProviderFactory::editEmailCondition,
                        DeclarativeUserProfileProviderFactory::readEmailCondition,
                        new AttributeValidatorMetadata(BlankAttributeValidator.ID, BlankAttributeValidator.createConfig(Messages.MISSING_EMAIL, false)),
                        new AttributeValidatorMetadata(DuplicateEmailValidator.ID),
                        new AttributeValidatorMetadata(EmailExistsAsUsernameValidator.ID),
                        new AttributeValidatorMetadata(EmailValidator.ID, ValidatorConfig.builder().config(EmailValidator.IGNORE_EMPTY_VALUE, true).build()))
                .setAttributeDisplayName("${email}")
                .setAnnotationDecorator(DeclarativeUserProfileProviderFactory::getEmailAnnotationDecorator);

        List<AttributeValidatorMetadata> readonlyValidators = new ArrayList<>();

        readonlyValidators.add(createReadOnlyAttributeUnchangedValidator(readOnlyAttributesPattern));

        if (readOnlyValidator != null) {
            readonlyValidators.add(readOnlyValidator);
        }

        metadata.addAttribute(READ_ONLY_ATTRIBUTE_KEY, 1000, readonlyValidators);

        return metadata;
    }

    /** 构建 Admin User API（USER_API）校验用 Profile。 */
    private UserProfileMetadata createUserResourceValidation(Config.Scope config) {
        Pattern p = getRegexPatternString(config.getArray(CONFIG_ADMIN_READ_ONLY_ATTRIBUTES));
        UserProfileMetadata metadata = new UserProfileMetadata(USER_API);


        metadata.addAttribute(UserModel.USERNAME, -2,
                        new AttributeValidatorMetadata(UsernameHasValueValidator.ID),
                        new AttributeValidatorMetadata(DuplicateUsernameValidator.ID))
                .addWriteCondition(DeclarativeUserProfileProviderFactory::editUsernameCondition);
        metadata.addAttribute(UserModel.EMAIL, -1,
                        new AttributeValidatorMetadata(DuplicateEmailValidator.ID),
                        new AttributeValidatorMetadata(EmailExistsAsUsernameValidator.ID),
                        new AttributeValidatorMetadata(EmailValidator.ID, ValidatorConfig.builder().config(EmailValidator.IGNORE_EMPTY_VALUE, true).build()))
                .addWriteCondition(DeclarativeUserProfileProviderFactory::editEmailCondition);

        List<AttributeValidatorMetadata> readonlyValidators = new ArrayList<>();

        if (p != null) {
            readonlyValidators.add(createReadOnlyAttributeUnchangedValidator(p));
        }

        readonlyValidators.add(createReadOnlyAttributeUnchangedValidator(adminReadOnlyAttributesPattern));
        metadata.addAttribute(READ_ONLY_ATTRIBUTE_KEY, 1000, readonlyValidators);

        metadata.addAttribute(UserModel.LOCALE, -1, DeclarativeUserProfileProviderFactory::isInternationalizationEnabled, DeclarativeUserProfileProviderFactory::isInternationalizationEnabled)
                .setRequired(AttributeMetadata.ALWAYS_FALSE);
        metadata.addAttribute(UserModel.EMAIL_PENDING, -1, this::isUpdateEmailFeatureEnabled, this::isUpdateEmailFeatureEnabled)
                .setAttributeDisplayName("${emailPendingVerification}")
                .setRequired(AttributeMetadata.ALWAYS_FALSE);

        metadata.addAttribute(TermsAndConditions.USER_ATTRIBUTE, -1, AttributeMetadata.ALWAYS_FALSE,
                        DeclarativeUserProfileProviderFactory::isTermAndConditionsEnabled)
                .setAttributeDisplayName("${termsAndConditionsUserAttribute}")
                .setRequired(AttributeMetadata.ALWAYS_FALSE);

        addAttributeUserDid(metadata);

        return metadata;
    }

    /** 构建账户管理（ACCOUNT）上下文 Profile。 */
    private UserProfileMetadata createAccountProfile(AttributeValidatorMetadata readOnlyValidator) {
        UserProfileMetadata defaultProfile = createDefaultProfile(UserProfileContext.ACCOUNT, readOnlyValidator);

        defaultProfile.addAttribute(UserModel.LOCALE, -1, DeclarativeUserProfileProviderFactory::isInternationalizationEnabled, DeclarativeUserProfileProviderFactory::isInternationalizationEnabled)
                .setRequired(AttributeMetadata.ALWAYS_FALSE);

        addAttributeUserDid(defaultProfile);

        return defaultProfile;
    }

    // 内部字段访问器

    /** @return 当前解析后的 UPConfig */
    protected UPConfig getParsedDefaultRawConfig() {
        return parsedConfig;
    }

    /** @return 上下文元数据注册表 */
    protected Map<UserProfileContext, UserProfileMetadata> getContextualMetadataRegistry() {
        return contextualMetadataRegistry;
    }

    private void initDefaultConfiguration(Scope config) {

        // 用户自定义配置在 init 时解析，应尽量避免运行时重复解析
        // 未设置用户配置时使用系统默认配置
        // Quarkus 可在构建时注入系统默认配置以优化启动
        parsedConfig = ofNullable(config.get("configFile"))
                .map(Paths::get)
                .map(UPConfigUtils::parseConfig)
                .orElse(PARSED_DEFAULT_RAW_CONFIG);

        // 兜底：解析 classpath 中的系统默认配置
        if (parsedConfig == null) {
            parsedConfig = UPConfigUtils.parseSystemDefaultConfig();
        }
    }

    /** 为邮箱字段附加 kc.required.action.supported 等 UI 注解。 */
    private static Map<String, Object> getEmailAnnotationDecorator(AttributeContext c) {
        AttributeMetadata m = c.getMetadata();
        Map<String, Object> rawAnnotations = Optional.ofNullable(m.getAnnotations()).orElse(Map.of());

        KeycloakSession session = c.getSession();
        KeycloakContext context = session.getContext();

        if (UpdateEmail.isEnabled(context.getRealm())) {
            UserProfileProvider provider = session.getProvider(UserProfileProvider.class);
            UPConfig upConfig = provider.getConfiguration();
            UPAttribute attribute = upConfig.getAttribute(UserModel.EMAIL);
            UPAttributePermissions permissions = attribute.getPermissions();

            if (permissions == null) {
                return rawAnnotations;
            }

            Set<String> writePermissions = permissions.getEdit();
            boolean isWritable = writePermissions.contains(UPConfigUtils.ROLE_USER);
            RealmModel realm = context.getRealm();

            if ((realm.isRegistrationEmailAsUsername() && !realm.isEditUsernameAllowed()) || !isWritable) {
                return rawAnnotations;
            }

            Map<String, Object> annotations = new HashMap<>(rawAnnotations);

            annotations.put("kc.required.action.supported", isWritable);

            return annotations;
        }

        return rawAnnotations;
    }

    /** 领域启用 UPDATE_EMAIL 且属性有值时展示 emailPending。 */
    private boolean isUpdateEmailFeatureEnabled(AttributeContext context) {
        Entry<String, List<String>> attribute = context.getAttribute();

        if (attribute.getValue().isEmpty()) {
            return false;
        }

        KeycloakSession session = context.getSession();
        KeycloakContext context1 = session.getContext();
        RealmModel realm = context1.getRealm();

        return UpdateEmail.isEnabled(realm);
    }

    /** 领域是否启用可验证凭证（DID 属性）。 */
    private static boolean isVerifiableCredentialsEnabled(AttributeContext context) {
        RealmModel realm = context.getSession().getContext().getRealm();
        return realm.isVerifiableCredentialsEnabled();
    }

    /** 在 Profile 中注册用户 DID 属性及格式/重复校验。 */
    private void addAttributeUserDid(UserProfileMetadata metadata) {
        Predicate<AttributeContext> required = AttributeMetadata.ALWAYS_FALSE;
        Predicate<AttributeContext> selector = DeclarativeUserProfileProviderFactory::isVerifiableCredentialsEnabled;
        Predicate<AttributeContext> readAllowed = DeclarativeUserProfileProviderFactory::isVerifiableCredentialsEnabled;
        Predicate<AttributeContext> writeAllowed = c -> isVerifiableCredentialsEnabled(c) && USER_API.equals(c.getContext());
        AttributeValidatorMetadata patternValidator = new AttributeValidatorMetadata(PatternValidator.ID, new ValidatorConfig(Map.of(
                "pattern", "^did:[a-z0-9]+:\\S+$",
                "error-message", "Value must follow the format 'did:method:identifier'",
                "ignore.empty.value", "true")));
        AttributeValidatorMetadata duplicateValidator = new AttributeValidatorMetadata(DuplicateDidValidator.ID);
        metadata.addAttribute(UserModel.DID, 10, List.of(patternValidator, duplicateValidator), selector, writeAllowed, required, readAllowed).setAttributeDisplayName("${did}");
    }
}
