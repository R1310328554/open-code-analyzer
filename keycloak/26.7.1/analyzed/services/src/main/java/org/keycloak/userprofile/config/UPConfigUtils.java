/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.userprofile.config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.util.StreamUtil;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.userprofile.UserProfileConstants;
import org.keycloak.util.JsonSerialization;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationResult;
import org.keycloak.validate.ValidatorConfig;
import org.keycloak.validate.Validators;

import static org.keycloak.common.util.ObjectUtil.isBlank;
import static org.keycloak.userprofile.UserProfileUtil.isRootAttribute;

/**
 * 用户 Profile 配置（{@link UPConfig}）读写与校验工具类。
 *
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class UPConfigUtils {

    /** classpath 默认配置文件路径。 */
    private static final String SYSTEM_DEFAULT_CONFIG_RESOURCE = "keycloak-default-user-profile.json";
    /** 伪角色：终端用户。 */
    public static final String ROLE_USER = UserProfileConstants.ROLE_USER;
    /** 伪角色：管理员。 */
    public static final String ROLE_ADMIN = UserProfileConstants.ROLE_ADMIN;

    private static final Set<String> PSEUDOROLES = new HashSet<>();
    /** 合法属性名正则（字母数字及 . _ -）。 */
    public static final Pattern ATTRIBUTE_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9\\._\\-]+");

    static {
        PSEUDOROLES.add(ROLE_ADMIN);
        PSEUDOROLES.add(ROLE_USER);
    }


    /**
     * 从 JSON 输入流加载配置（不做校验）。
     * <p>校验请使用 {@link #validate(KeycloakSession, UPConfig)}。</p>
     *
     * @param is JSON 输入流
     * @return 配置对象
     * @throws IOException JSON 解析失败时抛出
     */
    public static UPConfig readConfig(InputStream is) throws IOException {
        return JsonSerialization.readValue(is, UPConfig.class);
    }

    /**
     * 从 JSON 字符串解析用户 Profile 配置。
     *
     * @param rawConfig JSON 字符串
     * @return 配置对象
     * @throws IOException JSON 格式错误时抛出
     */
    public static UPConfig parseConfig(String rawConfig) throws IOException {
        return readConfig(new ByteArrayInputStream(rawConfig.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 校验 UPConfig：属性名/校验器/权限/分组/根属性等。
     * <ul>
     * <li>校验器 SPI 存在且配置合法</li>
     * <li>属性分组引用有效且组名非空</li>
     * <li>username/email 不可删除</li>
     * </ul>
     *
     * @param session 用于 Validator SPI
     * @param config 待校验配置
     * @return 错误消息列表，无错误时为空
     */
    public static List<String> validate(KeycloakSession session, UPConfig config) {
        List<String> errors = validateAttributes(session, config);
        errors.addAll(validateAttributeGroups(config));
        return errors;
    }

    private static List<String> validateAttributeGroups(UPConfig config) {
        long groupsWithoutName = config.getGroups().stream().filter(g -> g.getName() == null).collect(Collectors.counting());

        if (groupsWithoutName > 0) {
            String errorMessage = "Name is mandatory for groups, found " + groupsWithoutName + " group(s) without name.";
            return Collections.singletonList(errorMessage);
        }
        return Collections.emptyList();
    }

    private static List<String> validateAttributes(KeycloakSession session, UPConfig config) {
        List<String> errors = new ArrayList<>();
        Set<String> groups = config.getGroups().stream()
                .map(g -> g.getName())
                .collect(Collectors.toSet());

        if (config.getAttributes() != null) {
            Set<String> attNamesCache = new HashSet<>();
            config.getAttributes().forEach((attribute) -> validateAttribute(session, attribute, groups, errors, attNamesCache));
            errors.addAll(validateRootAttributes(config));
        }

        return errors;
    }

    private static List<String> validateRootAttributes(UPConfig config) {
        List<UPAttribute> attributes = config.getAttributes();

        if (attributes == null) {
            return Collections.emptyList();
        }

        List<String> errors = new ArrayList<>();
        List<String> attributeNames = attributes.stream().map(UPAttribute::getName).toList();

        for (String name : Arrays.asList(UserModel.USERNAME, UserModel.EMAIL)) {
            if (!attributeNames.contains(name)) {
                errors.add("The attribute '" + name + "' can not be removed");
            }
        }

        return errors;
    }

    /**
     * 校验单个属性配置（名称唯一、校验器、权限、分组、注解等）。
     *
     * @param session Validator SPI 会话
     * @param attributeConfig 属性配置
     * @param groups 已定义分组名集合
     * @param errors 累积错误列表
     * @param attNamesCache 已见属性名缓存（唯一性）
     */
    private static void validateAttribute(KeycloakSession session, UPAttribute attributeConfig, Set<String> groups, List<String> errors, Set<String> attNamesCache) {
        String attributeName = attributeConfig.getName();
        if (isBlank(attributeName)) {
            errors.add("Attribute configuration without 'name' is not allowed");
        } else {
            if (attNamesCache.contains(attributeName)) {
                errors.add("Attribute configuration already exists with 'name':'" + attributeName + "'");
            } else {
                attNamesCache.add(attributeName);
                if(!isValidAttributeName(attributeName)) {
                    errors.add("Invalid attribute name (only letters, numbers and '.' '_' '-' special characters allowed): " + attributeName + "'");
                }
            }
        }
        if (attributeConfig.getValidations() != null) {
            attributeConfig.getValidations().forEach((validator, validatorConfig) -> validateValidationConfig(session, validator, validatorConfig, attributeName, errors));
            validateDefaultValue(session, attributeConfig, errors);
        }
        if (attributeConfig.getPermissions() != null) {
            if (attributeConfig.getPermissions().getView() != null) {
                validateRoles(attributeConfig.getPermissions().getView(), "permissions.view", errors, attributeName);
            }
            if (attributeConfig.getPermissions().getEdit() != null) {
                validateRoles(attributeConfig.getPermissions().getEdit(), "permissions.edit", errors, attributeName);
            }
        }
        if (attributeConfig.getRequired() != null) {
            validateRoles(attributeConfig.getRequired().getRoles(), "required.roles", errors, attributeName);
            validateScopes(attributeConfig.getRequired().getScopes(), "required.scopes", attributeName, errors, session);
        }
        if (attributeConfig.getSelector() != null) {
            validateScopes(attributeConfig.getSelector().getScopes(), "selector.scopes", attributeName, errors, session);
        }

        if (attributeConfig.getGroup() != null) {
            if (!groups.contains(attributeConfig.getGroup())) {
                errors.add("Attribute '" + attributeName + "' references unknown group '" + attributeConfig.getGroup() + "'");
            }
        }

        if (attributeConfig.getAnnotations()!=null) {
            validateAnnotations(attributeConfig.getAnnotations(), errors, attributeName);
        }
    }

    private static void validateDefaultValue(KeycloakSession session, UPAttribute attributeConfig, List<String> errors) {
        String defaultValue = attributeConfig.getDefaultValue();

        if (defaultValue == null) {
            return;
        }

        String attributeName = attributeConfig.getName();

        if (isRootAttribute(attributeName)) {
            errors.add("Default value not supported for attribute '" + attributeName + "'");
        } else {
            attributeConfig.getValidations().forEach((validator, validatorConfig) -> {
                ValidationContext context = Validators.validator(session, validator).validate(defaultValue, attributeName, ValidatorConfig.configFromMap(validatorConfig));
                if (!context.isValid()) {
                    errors.add("Default value for attribute '" + attributeName + "' is invalid");
                }
            });
        }
    }

    private static void validateAnnotations(Map<String, Object> annotations, List<String> errors, String attributeName) {
        if (annotations.containsKey("inputOptions") && !(annotations.get("inputOptions") instanceof List)) {
            errors.add(new StringBuilder("Annotation 'inputOptions' configured for attribute '").append(attributeName).append("' must be an array of values!'").toString());
        }
        if (annotations.containsKey("inputOptionLabels") && !(annotations.get("inputOptionLabels") instanceof Map)) {
            errors.add(new StringBuilder("Annotation 'inputOptionLabels' configured for attribute '").append(attributeName).append("' must be an object!'").toString());
        }
    }

    private static void validateScopes(Set<String> scopes, String propertyName, String attributeName, List<String> errors, KeycloakSession session) {
        if (scopes == null) {
            return;
        }

        for (String scope : scopes) {
            RealmModel realm = session.getContext().getRealm();
            Stream<ClientScopeModel> realmScopes = realm.getClientScopesStream();

            if (!realmScopes.anyMatch(cs -> cs.getName().equals(scope))) {
                errors.add(new StringBuilder("'").append(propertyName).append("' configuration for attribute '").append(attributeName).append("' contains unsupported scope '").append(scope).append("'").toString());
            }
        }
    }

    /**
     * 判断属性名是否符合 {@link #ATTRIBUTE_NAME_PATTERN}。
     *
     * @param attributeName 待校验属性名
     * @return 合法为 true
     */
    public static boolean isValidAttributeName(String attributeName) {
        return ATTRIBUTE_NAME_PATTERN.matcher(attributeName).matches();
    }

    /**
     * 校验配置角色列表是否均为 {@link #PSEUDOROLES} 中的伪角色。
     *
     * @param roles 角色集合
     * @param fieldName 错误消息中的字段名
     * @param errors 错误列表
     * @param attributeName 属性名
     */
    private static void validateRoles(Set<String> roles, String fieldName, List<String> errors, String attributeName) {
        if (roles != null) {
            for (String role : roles) {
                if (!PSEUDOROLES.contains(role)) {
                    errors.add("'" + fieldName + "' configuration for attribute '" + attributeName + "' contains unsupported role '" + role + "'");
                }
            }
        }
    }

    /**
     * 校验属性上配置的校验器 ID 存在且 ValidatorConfig 合法。
     *
     * @param session Validator SPI 会话
     * @param validatorConfig 校验器配置映射
     * @param errors 错误列表
     */
    private static void validateValidationConfig(KeycloakSession session, String validator, Map<String, Object> validatorConfig, String attributeName, List<String> errors) {

        if (isBlank(validator)) {
            errors.add("Validation without validator id is defined for attribute '" + attributeName + "'");
        } else {
            if(session!=null) {
                if(Validators.validator(session, validator) == null) {
                    errors.add("Validator '" + validator + "' defined for attribute '" + attributeName + "' doesn't exist");
                } else {
                    ValidationResult result = Validators.validateConfig(session, validator, ValidatorConfig.configFromMap(validatorConfig));
                    if(!result.isValid()) {
                        final StringBuilder sb = new StringBuilder();
                        result.forEachError(err -> sb.append(err.toString()+", "));
                        errors.add("Validator '" + validator + "' defined for attribute '" + attributeName + "' has incorrect configuration: " + sb.toString());
                    }
                }
            }
        }
    }

    /** 将字符串首字母大写。 */
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /** 读取 classpath 默认用户 Profile JSON 文本。 */
    public static String readSystemDefaultConfig() {
        try (InputStream is = getSystemDefaultConfig()) {
            return StreamUtil.readString(is, Charset.defaultCharset());
        } catch (IOException cause) {
            throw new RuntimeException("Failed to load default user profile config file", cause);
        }
    }

    /** 解析系统默认 UPConfig。 */
    public static UPConfig parseSystemDefaultConfig() {
        return parseConfig(getSystemDefaultConfig());
    }

    /** 从文件路径解析 UPConfig。 */
    public static UPConfig parseConfig(Path configPath) {
        if (configPath == null) {
            throw new IllegalArgumentException("Null configPath");
        }

        try (InputStream is = new FileInputStream(configPath.toFile())) {
            return parseConfig(is);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to read default user profile configuration: " + configPath, ioe);
        }
    }

    private static UPConfig parseConfig(InputStream is) {
        try {
            return JsonSerialization.readValue(is, UPConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse default user profile configuration stream", e);
        }
    }

    private static InputStream getSystemDefaultConfig() {
        return UPConfigUtils.class.getResourceAsStream(SYSTEM_DEFAULT_CONFIG_RESOURCE);
    }
}
