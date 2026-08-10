/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.userprofile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPAttributePermissions;
import org.keycloak.representations.userprofile.config.UPAttributeRequired;
import org.keycloak.representations.userprofile.config.UPAttributeSelector;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.representations.userprofile.config.UPGroup;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.userprofile.config.DeclarativeUserProfileModel;
import org.keycloak.userprofile.config.UPConfigUtils;
import org.keycloak.userprofile.validator.AttributeRequiredByMetadataValidator;
import org.keycloak.userprofile.validator.ImmutableAttributeValidator;
import org.keycloak.userprofile.validator.MultiValueValidator;
import org.keycloak.util.JsonSerialization;
import org.keycloak.validate.AbstractSimpleValidator;
import org.keycloak.validate.ValidatorConfig;

import static org.keycloak.common.util.ObjectUtil.isBlank;
import static org.keycloak.protocol.oidc.TokenManager.getRequestedClientScopes;

/**
 * 声明式 {@link UserProfileProvider}：从组件配置中的可变更 JSON 加载用户档案配置，解析结果会缓存。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class DeclarativeUserProfileProvider implements UserProfileProvider {

    /** 组件配置中存储 UP JSON 的键。 */
    public static final String UP_COMPONENT_CONFIG_KEY = "kc.user.profile.config";
    protected static final String PARSED_CONFIG_COMPONENT_KEY = "kc.user.profile.metadata";
    protected static final String PARSED_UP_CONFIG_COMPONENT_KEY = "kc.parsed.up.config";

    /**
     * 判断当前认证流是否请求了配置中的任一 scope；用户管理 API 上下文下始终返回 true。
     *
     * @param context 属性上下文，用于获取当前认证流
     * @param configuredScopes 待匹配的 scope 集合
     */
    private static boolean requestedScopePredicate(AttributeContext context, Set<String> configuredScopes) {
        // 通过 User Admin API 管理时，任意属性均可用
        if (UserProfileContext.USER_API.equals(context.getContext())) {
            return true;
        }

        KeycloakSession session = context.getSession();
        String requestedScopes = AuthenticationManager.getRequestedScopes(session);
        ClientModel client = session.getContext().getClient();

        return getRequestedClientScopes(session, requestedScopes, client, context.getUser())
                .map(ClientScopeModel::getName)
                .anyMatch(configuredScopes::contains);
    }

    private final KeycloakSession session;
    private final String providerId;
    private final Map<UserProfileContext, UserProfileMetadata> contextualMetadataRegistry;
    protected final UPConfig parsedDefaultRawConfig;

    public DeclarativeUserProfileProvider(KeycloakSession session, DeclarativeUserProfileProviderFactory factory) {
        this.session = session;
        this.providerId = factory.getId();
        this.contextualMetadataRegistry = factory.getContextualMetadataRegistry();
        this.parsedDefaultRawConfig = factory.getParsedDefaultRawConfig();
    }

    /** 服务账号用户使用专用 {@link ServiceAccountAttributes}。 */
    protected Attributes createAttributes(UserProfileContext context, Map<String, ?> attributes,
            UserModel user, UserProfileMetadata metadata) {

        if (isServiceAccountUser(user)) {
            return new ServiceAccountAttributes(context, attributes, user, metadata, session);
        }
        return new DefaultAttributes(context, attributes, user, metadata, session);
    }

    @Override
    public UserProfile create(UserProfileContext context, UserModel user) {
        return createUserProfile(context, user.getAttributes(), user);
    }

    @Override
    public UserProfile create(UserProfileContext context, Map<String, ?> attributes, UserModel user) {
        return createUserProfile(context, attributes, user);
    }

    @Override
    public UserProfile create(UserProfileContext context, Map<String, ?> attributes) {
        return createUserProfile(context, attributes, null);
    }

    private UserProfile createUserProfile(UserProfileContext context, Map<String, ?> attributes, UserModel user) {
        UserProfileMetadata defaultMetadata = contextualMetadataRegistry.get(context);

        if (defaultMetadata == null) {
            // 部分上下文及其元数据仅在对应特性启用时可用
            throw new RuntimeException("No metadata is bound to the " + context + " context");
        }

        UserProfileMetadata metadata = configureUserProfile(defaultMetadata, session);
        Attributes profileAttributes = createAttributes(context, attributes, user, metadata);
        return new DefaultUserProfile(metadata, profileAttributes, createUserFactory(), user, session);
    }

    /**
     * 返回用于 {@link UserProfile#create()} 创建新用户的工厂函数。
     *
     * @return 创建 {@link UserModel} 的函数
     */
    private Function<Attributes, UserModel> createUserFactory() {
        return new Function<>() {
            private UserModel user;

            @Override
            public UserModel apply(Attributes attributes) {
                if (user == null) {
                    String userName = attributes.getFirst(UserModel.USERNAME);

                    // 若允许 email 作为用户名则回退到 email
                    if (userName == null) {
                        userName = attributes.getFirst(UserModel.EMAIL);
                    }

                    user = session.users().addUser(session.getContext().getRealm(), userName);
                }

                return user;
            }
        };
    }

    /**
     * 按 realm 组件配置在运行时装饰上下文元数据，结果缓存在组件 note 中。
     *
     * @param metadata 基础档案元数据
     * @return 装饰后的元数据
     */
    protected UserProfileMetadata configureUserProfile(UserProfileMetadata metadata, KeycloakSession session) {
        UserProfileContext context = metadata.getContext();
        UserProfileMetadata decoratedMetadata = metadata.clone();
        ComponentModel component = getComponentModel().orElse(null);

        if (component == null) {
            return decoratedMetadata;
        }

        Map<UserProfileContext, UserProfileMetadata> metadataMap = component.getNote(PARSED_CONFIG_COMPONENT_KEY);

        // 未缓存则创建 note 并写入
        if (metadataMap == null) {
            metadataMap = new ConcurrentHashMap<>();
            component.setNote(PARSED_CONFIG_COMPONENT_KEY, metadataMap);
        }

        return metadataMap.computeIfAbsent(context, createUserDefinedProfileDecorator(session, decoratedMetadata, component)).clone();
    }

    @Override
    public UPConfig getConfiguration() {
        Optional<ComponentModel> component = getComponentModel();

        if (component.isPresent()) {
            UPConfig cfg = getConfigFromComponentModel(component.get());

            if (cfg == null) {
                cfg = parsedDefaultRawConfig;
            }

            return cfg.clone();
        }

        return parsedDefaultRawConfig.clone();
    }

    @Override
    public void setConfiguration(UPConfig configuration) {
        RealmModel realm = session.getContext().getRealm();
        Optional<ComponentModel> optionalComponent = getComponentModel();

        // 避免创建组件后立即删除
        if (optionalComponent.isEmpty() && configuration == null) {
            return;
        }

        ComponentModel component = optionalComponent.orElseGet(this::createComponentModel);

        removeConfigJsonFromComponentModel(component);

        if (configuration == null) {
            realm.removeComponent(component);
            return;
        }

        try {
            component.getConfig().putSingle(UP_COMPONENT_CONFIG_KEY, JsonSerialization.writeValueAsString(configuration));
        } catch (IOException ioe) {
            throw new RuntimeException("Cannot write component config", ioe);
        }

        realm.updateComponent(component);
    }

    private Optional<ComponentModel> getComponentModel() {
        RealmModel realm = session.getContext().getRealm();
        return realm.getComponentsStream(realm.getId(), UserProfileProvider.class.getName()).filter(componentModel -> componentModel.getProviderId().equals(providerId)).findFirst();
    }

    /**
     * 根据 realm 级 UP 配置装饰基础元数据；每个 {@link UserProfileContext} 在 realm 内调用一次并缓存。
     */
    protected UserProfileMetadata decorateUserProfileForCache(UserProfileMetadata decoratedMetadata, UPConfig parsedConfig) {
        UserProfileContext context = decoratedMetadata.getContext();

        if (parsedConfig == null || parsedConfig.getAttributes() == null) {
            return decoratedMetadata;
        }

        Map<String, UPGroup> groupsByName = asHashMap(parsedConfig.getGroups());
        int guiOrder = 0;

        for (UPAttribute attrConfig : parsedConfig.getAttributes()) {
            String attributeName = attrConfig.getName();

            if (!context.isAttributeSupported(attributeName)) {
                // 当前上下文不支持的属性将被忽略（如 UPDATE_EMAIL 上下文仅支持 email）
                continue;
            }

            List<AttributeValidatorMetadata> validators = new ArrayList<>();
            Map<String, Map<String, Object>> validationsConfig = attrConfig.getValidations();

            if (validationsConfig != null) {
                for (Map.Entry<String, Map<String, Object>> vc : validationsConfig.entrySet()) {
                    validators.add(createConfiguredValidator(vc.getKey(), vc.getValue()));
                }
            }

            UPAttributeRequired rc = attrConfig.getRequired();
            if (rc != null) {
                validators.add(new AttributeValidatorMetadata(AttributeRequiredByMetadataValidator.ID));
            }

            Predicate<AttributeContext> required = AttributeMetadata.ALWAYS_FALSE;
            if (rc != null) {
                if (rc.isAlways() || context.isRoleForContext(rc.getRoles())) {
                    // 服务账号不需要常规必填属性
                    required = c -> !isServiceAccountUser(c.getUser());

                    // 若配置了 scope，则仅在当前 AuthenticationSession 包含对应 scope 时必填
                    if (rc.getScopes() != null && !rc.getScopes().isEmpty()) {
                        if (context.canBeAuthFlowContext()) {
                            required = (c) -> !isServiceAccountUser(c.getUser()) && requestedScopePredicate(c, rc.getScopes());
                        } else {
                            // 管理端与账户上下文不支持 scope
                            required = AttributeMetadata.ALWAYS_FALSE;
                        }
                    }
                } else if (context.canBeAuthFlowContext() && rc.getScopes() != null && !rc.getScopes().isEmpty()) {
                    // 认证流上下文且配置了 scope 必填时，创建基于 scope 的选择器
                    required = (c) -> !isServiceAccountUser(c.getUser()) && requestedScopePredicate(c, rc.getScopes());
                }
            }

            Predicate<AttributeContext> writeAllowed = AttributeMetadata.ALWAYS_FALSE;
            Predicate<AttributeContext> readAllowed = AttributeMetadata.ALWAYS_FALSE;
            UPAttributePermissions permissions = attrConfig.getPermissions();

            if (permissions != null) {
                Set<String> editRoles = permissions.getEdit();

                if (!editRoles.isEmpty()) {
                    writeAllowed = ac -> ac.getContext().isRoleForContext(editRoles);
                }

                Set<String> viewRoles = permissions.getView();

                if (viewRoles.isEmpty()) {
                    readAllowed = writeAllowed;
                } else {
                    readAllowed = createViewAllowedPredicate(writeAllowed, viewRoles);
                }
            }

            Predicate<AttributeContext> selector = AttributeMetadata.ALWAYS_TRUE;
            UPAttributeSelector sc = attrConfig.getSelector();
            if (sc != null && !isBuiltInAttribute(context, attributeName) && context.canBeAuthFlowContext() && sc.getScopes() != null && !sc.getScopes().isEmpty()) {
                // 认证流上下文且配置了 scope 选择器时构建对应谓词
                selector = (c) -> requestedScopePredicate(c, sc.getScopes());
            }

            Map<String, Object> annotations = attrConfig.getAnnotations();
            String attributeGroup = attrConfig.getGroup();
            AttributeGroupMetadata groupMetadata = toAttributeGroupMeta(groupsByName.get(attributeGroup));

            guiOrder++;

            validators.add(new AttributeValidatorMetadata(ImmutableAttributeValidator.ID));

            // 非多值托管属性限制为单值
            if (!attrConfig.isMultivalued() && validators.stream().map(AttributeValidatorMetadata::getValidatorId).noneMatch(MultiValueValidator.ID::equals)) {
                validators.add(new AttributeValidatorMetadata(MultiValueValidator.ID, ValidatorConfig.builder()
                        .config("max", "1")
                        .build()));
            }

            if (isBuiltInAttribute(context, attributeName)) {
                // 内置属性未配置权限时默认可读写
                if (permissions == null || permissions.isEmpty()) {
                    writeAllowed = AttributeMetadata.ALWAYS_TRUE;
                    readAllowed = AttributeMetadata.ALWAYS_TRUE;
                }

                if (UserModel.USERNAME.equals(attributeName)) {
                    required = new UsernameRequiredPredicate();
                }

                if (UserModel.EMAIL.equals(attributeName)) {
                    required = new EmailRequiredPredicate(required);
                }

                List<AttributeMetadata> existingMetadata = decoratedMetadata.getAttribute(attributeName);

                if (existingMetadata.isEmpty()) {
                    throw new IllegalStateException("Attribute " + attributeName + " not defined in the context.");
                }

                for (AttributeMetadata metadata : existingMetadata) {
                    metadata.addAnnotations(annotations)
                            .setAttributeDisplayName(attrConfig.getDisplayName())
                            .setGuiOrder(guiOrder)
                            .setAttributeGroupMetadata(groupMetadata)
                            .addReadCondition(readAllowed)
                            .addWriteCondition(writeAllowed)
                            .addValidators(validators)
                            .setRequired(required)
                            .setDefaultValue(attrConfig.getDefaultValue())
                            .setMultivalued(attrConfig.isMultivalued());
                }
            } else {
                decoratedMetadata.addAttribute(attributeName, guiOrder, validators, selector, writeAllowed, required, readAllowed)
                        .addAnnotations(annotations)
                        .setAttributeDisplayName(attrConfig.getDisplayName())
                        .setAttributeGroupMetadata(groupMetadata)
                        .setDefaultValue(attrConfig.getDefaultValue())
                        .setMultivalued(attrConfig.isMultivalued());
            }
        }

        return decoratedMetadata;

    }

    private Map<String, UPGroup> asHashMap(List<UPGroup> groups) {
        return groups.stream().collect(Collectors.toMap(UPGroup::getName, g -> g));
    }

    private AttributeGroupMetadata toAttributeGroupMeta(UPGroup group) {
        if (group == null) {
            return null;
        }
        return new AttributeGroupMetadata(group.getName(), group.getDisplayHeader(), group.getDisplayDescription(), group.getAnnotations());
    }

    private boolean isBuiltInAttribute(UserProfileContext context, String attributeName) {
        if (UserProfileContext.SCIM.equals(context)) {
            if (UserModel.FIRST_NAME.equals(attributeName) || UserModel.LAST_NAME.equals(attributeName)) {
                return true;
            }
        }
        return UserModel.USERNAME.equals(attributeName) || UserModel.EMAIL.equals(attributeName);
    }

    private boolean isOptionalBuiltInAttribute(String attributeName) {
        return UserModel.FIRST_NAME.equals(attributeName) || UserModel.LAST_NAME.equals(attributeName);
    }

    private Predicate<AttributeContext> createViewAllowedPredicate(Predicate<AttributeContext> canEdit,
            Set<String> viewRoles) {
        return ac -> ac.getContext().isRoleForContext(viewRoles) || canEdit.test(ac);
    }

    private static boolean isServiceAccountUser(UserModel user) {
        return user != null && user.getServiceAccountClientLink() != null;
    }

    /**
     * 从组件模型读取并解析 UP 配置；未配置时使用默认配置。
     */
    protected UPConfig parseConfigOrDefault(ComponentModel component) {
        String rawConfig = component.get(UP_COMPONENT_CONFIG_KEY);

        if (isBlank(rawConfig)) {
            return parsedDefaultRawConfig;
        }

        try {
            return UPConfigUtils.parseConfig(rawConfig);
        } catch (IOException e) {
            throw new RuntimeException("UserProfile configuration for realm '" + session.getContext().getRealm().getName() + "' is invalid:" + e.getMessage(), e);
        }
    }

    /**
     * 创建用于持久化 UP 配置的组件模型。
     *
     * @return 新组件模型
     */
    protected ComponentModel createComponentModel() {
        RealmModel realm = session.getContext().getRealm();
        return realm.addComponentModel(new DeclarativeUserProfileModel(providerId));
    }

    /**
     * 根据用户档案配置创建验证器元数据。
     *
     * @param validator 验证器 ID
     * @param validatorConfig 验证器配置参数
     * @return 可执行的验证器元数据
     */
    protected AttributeValidatorMetadata createConfiguredValidator(String validator, Map<String, Object> validatorConfig) {
        return new AttributeValidatorMetadata(validator, ValidatorConfig.builder().config(validatorConfig).config(AbstractSimpleValidator.IGNORE_EMPTY_VALUE, true).build());
    }

    private UPConfig getConfigFromComponentModel(ComponentModel model) {
        UPConfig cached = getParsedConfigFromCache(model);

        if (cached == null) {
            cached = parseAndCacheConfig(model);
        }

        return cached;
    }

    private UPConfig parseAndCacheConfig(ComponentModel model) {
        UPConfig cfg = parseConfigOrDefault(model);
        model.setNote(PARSED_UP_CONFIG_COMPONENT_KEY, cfg);
        return cfg;
    }

    private UPConfig getParsedConfigFromCache(ComponentModel component) {
        if (component == null) {
            return null;
        }

        return component.getNote(PARSED_UP_CONFIG_COMPONENT_KEY);
    }

    private void removeConfigJsonFromComponentModel(ComponentModel model) {
        if (model == null)
            return;

        model.getConfig().remove(UP_COMPONENT_CONFIG_KEY);
    }

    @Override
    public void close() {
    }

    private Function<UserProfileContext, UserProfileMetadata> createUserDefinedProfileDecorator(KeycloakSession session, UserProfileMetadata decoratedMetadata, ComponentModel component) {
        return (c) -> {
            RealmModel realm = session.getContext().getRealm();
            UPConfig parsedConfig = getConfigFromComponentModel(component);

            // 校验配置以尽早发现验证器变更/移除等问题
            List<String> errors = UPConfigUtils.validate(session, parsedConfig);
            if (!errors.isEmpty()) {
                throw new RuntimeException("UserProfile configuration for realm '" + realm.getName() + "' is invalid: " + errors);
            }

            Iterator<AttributeMetadata> attributes = decoratedMetadata.getAttributes().iterator();

            while (attributes.hasNext()) {
                AttributeMetadata metadata = attributes.next();

                String attributeName = metadata.getName();

                if (isBuiltInAttribute(decoratedMetadata.getContext(), attributeName) && parsedDefaultRawConfig != null) {
                    UPAttribute upAttribute = parsedDefaultRawConfig.getAttribute(attributeName);
                    Map<String, Map<String, Object>> validations = Optional.ofNullable(upAttribute.getValidations()).orElse(Collections.emptyMap());

                    for (String id : validations.keySet()) {
                        List<AttributeValidatorMetadata> validators = metadata.getValidators();
                        // 内置属性的默认验证器不进入基础元数据，由用户配置重新添加
                        validators.removeIf(m -> m.getValidatorId().equals(id));
                    }
                } else if (isOptionalBuiltInAttribute(attributeName)) {
                    // 移除可选内置属性，由用户配置接管
                    // 确保除 username/email 外的属性从元数据中清除
                    attributes.remove();
                }
            }

            return decorateUserProfileForCache(decoratedMetadata, parsedConfig);
        };
    }

    /** 邮箱必填谓词：服务账号除外，且考虑“注册时 email 作为 username” realm 设置。 */
    private static class EmailRequiredPredicate implements Predicate<AttributeContext> {
        private final Predicate<AttributeContext> required;

        public EmailRequiredPredicate(Predicate<AttributeContext> required) {
            this.required = required;
        }

        @Override
        public boolean test(AttributeContext context) {
            UserModel user = context.getUser();

            if (isServiceAccountUser(user)) {
                return false;
            }

            if (required.test(context)) return true;

            RealmModel realm = context.getSession().getContext().getRealm();
            return realm.isRegistrationEmailAsUsername();
        }
    }

    /** 用户名必填谓词：当 realm 未启用“email 作为 username”时 username 必填。 */
    private static class UsernameRequiredPredicate implements Predicate<AttributeContext> {
        @Override
        public boolean test(AttributeContext context) {
            RealmModel realm = context.getSession().getContext().getRealm();
            return !realm.isRegistrationEmailAsUsername();
        }
    }
}
