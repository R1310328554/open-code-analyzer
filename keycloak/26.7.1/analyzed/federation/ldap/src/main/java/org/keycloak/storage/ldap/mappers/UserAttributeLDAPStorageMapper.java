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

package org.keycloak.storage.ldap.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.models.utils.reflection.Property;
import org.keycloak.storage.DatastoreProvider;
import org.keycloak.storage.StoreManagers;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.LDAPUtils;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPUtil;

import org.jboss.logging.Logger;

import static java.util.Optional.ofNullable;

/**
 * 用户属性 LDAP 存储映射器：将单个 LDAP 属性与 Keycloak {@link UserModel} 属性双向同步。
 * <p>
 * 支持只读、可写、二进制属性、默认值及从 LDAP 优先读取等模式，并通过 {@link TxAwareLDAPUserModelDelegate} 延迟写回 LDAP。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserAttributeLDAPStorageMapper extends AbstractLDAPStorageMapper {

    private static final Logger logger = Logger.getLogger(UserAttributeLDAPStorageMapper.class);

    private static final Map<String, Property<Object>> userModelProperties = LDAPUtils.getUserModelProperties();

    /** 配置键：Keycloak 用户模型属性名。 */
    public static final String USER_MODEL_ATTRIBUTE = "user.model.attribute";
    /** 配置键：LDAP 属性名。 */
    public static final String LDAP_ATTRIBUTE = "ldap.attribute";
    /** 配置键：是否只读（仅导入，不回写 LDAP）。 */
    public static final String READ_ONLY = "read.only";
    /** 配置键：读取时始终从 LDAP 取值。 */
    public static final String ALWAYS_READ_VALUE_FROM_LDAP = "always.read.value.from.ldap";
    /** 配置键：该属性在 LDAP 中是否必填。 */
    public static final String IS_MANDATORY_IN_LDAP = "is.mandatory.in.ldap";
    /** 配置键：是否为二进制 LDAP 属性。 */
    public static final String IS_BINARY_ATTRIBUTE = "is.binary.attribute";
    /** 配置键：二进制属性解码方式。 */
    public static final String BINARY_ATTRIBUTE_DECODER = "binary.attribute.decoder";
    /** 解码模式：自动（匹配 UUID 属性时解码为 UUID）。 */
    public static final String BINARY_DECODER_AUTO = "auto";
    /** 解码模式：始终 Base64 字符串。 */
    public static final String BINARY_DECODER_BASE64 = "base64";
    /** 解码模式：始终解码为 UUID/GUID。 */
    public static final String BINARY_DECODER_UUID = "uuid";
    /** 配置键：LDAP 必填时的默认属性值。 */
    public static final String ATTRIBUTE_DEFAULT_VALUE = "attribute.default.value";
    /** 配置键：未配置默认值时是否强制使用空默认值。 */
    public static final String FORCE_DEFAULT_VALUE = "attribute.force.default";

    /** 构造映射器并绑定组件模型与 LDAP 提供者。 */
    public UserAttributeLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
        super(mapperModel, ldapProvider);
    }

    /** 从 LDAP 导入用户时，将 LDAP 属性值写入 Keycloak 用户模型。 */
    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        String userModelAttrName = getUserModelAttribute();
        String ldapAttrName = getLdapAttributeName();

        // 二进制属性不写回 Keycloak 数据库，体积可能过大
        if (isBinaryAttribute()) {
            return;
        }

        Property<Object> userModelProperty = userModelProperties.get(userModelAttrName.toLowerCase());

        if (userModelProperty != null) {

            // UserModel 上存在对应 Java 属性
            String ldapAttrValue = ldapUser.getAttributeAsString(ldapAttrName);

            checkDuplicateEmail(userModelAttrName, ldapAttrValue, realm, ldapProvider.getSession(), user);

            setPropertyOnUserModel(userModelProperty, user, ldapAttrValue);
        } else {

            // 无 Java 属性，按自定义属性写入
            Set<String> ldapAttrValue = ldapUser.getAttributeAsSet(ldapAttrName);
            if (ldapAttrValue != null) {
                user.setAttribute(userModelAttrName, new ArrayList<>(ldapAttrValue));
            } else {
                user.removeAttribute(userModelAttrName);
            }
        }
    }

    /** 将 Keycloak 用户注册到 LDAP 时，把模型属性写入 LDAP 对象。 */
    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {
        String userModelAttrName = getUserModelAttribute();
        String ldapAttrName = getLdapAttributeName();
        boolean isMandatoryInLdap = parseBooleanParameter(mapperModel, IS_MANDATORY_IN_LDAP);
        String attributeDefaultValue = getAttributeDefaultValue();

        Property<Object> userModelProperty = userModelProperties.get(userModelAttrName.toLowerCase());

        if (userModelProperty != null) {

            // UserModel Java 属性，仅支持简单类型
            Object attrValue = userModelProperty.getValue(localUser);

            if (attrValue == null) {
                if (isMandatoryInLdap && attributeDefaultValue != null) {
                    ldapUser.setSingleAttribute(ldapAttrName, attributeDefaultValue);
                } else {
                    ldapUser.setAttribute(ldapAttrName, new LinkedHashSet<String>());
                }
            } else {
                ldapUser.setSingleAttribute(ldapAttrName, attrValue.toString());
            }
        } else {

            // 无 Java 属性，按多值属性写入 LDAP
            List<String> attrValues = localUser.getAttributeStream(userModelAttrName).collect(Collectors.toList());

            if (attrValues.isEmpty()) {
                if (isMandatoryInLdap && attributeDefaultValue != null) {
                    ldapUser.setSingleAttribute(ldapAttrName, attributeDefaultValue);
                } else {
                    ldapUser.setAttribute(ldapAttrName, new LinkedHashSet<>());
                }
            } else {
                ldapUser.setAttribute(ldapAttrName, new LinkedHashSet<>(attrValues));
            }
        }

        if (isReadOnly()) {
            ldapUser.addReadOnlyAttributeName(ldapAttrName);
        }
    }

    @Override
    public Set<String> mandatoryAttributeNames() {
        boolean isMandatoryInLdap = mapperModel.get(IS_MANDATORY_IN_LDAP, false);
        return isMandatoryInLdap? Collections.singleton(getLdapAttributeName()) : null;
    }

    @Override
    public Set<String> getUserAttributes() {
        return Collections.singleton(getUserModelAttribute());
    }

    /** 若域内已有其他用户使用相同邮箱，则抛出 {@link ModelDuplicateException}。 */
    protected void checkDuplicateEmail(String userModelAttrName, String email, RealmModel realm, KeycloakSession session, UserModel user) {
        if (email == null || realm.isDuplicateEmailsAllowed()) return;
        if (UserModel.EMAIL.equalsIgnoreCase(userModelAttrName)) {
            // lowercase before search
            email = KeycloakModelUtils.toLowerCaseSafe(email);

            UserModel that = UserStoragePrivateUtil.userLocalStorage(session).getUserByEmail(realm, email);

            if (that != null && !that.getId().equals(user.getId())) {
                // 通过 getUserById 触发校验：若 LDAP 联邦用户已不存在则从本地库移除
                that = ((StoreManagers) session.getProvider(DatastoreProvider.class)).userStorageManager().getUserById(realm, that.getId());
                if (that != null) {
                    session.getTransactionManager().setRollbackOnly();
                    String exceptionMessage = String.format("Can't import user '%s' from LDAP because email '%s' already exists in Keycloak. Existing user with this email is '%s'", user.getUsername(), email, that.getUsername());
                    throw new ModelDuplicateException(exceptionMessage, UserModel.EMAIL);
                }
            }
        }
    }

    /** 校验用户名变更是否违反域策略或与其他用户冲突。 */
    protected void checkDuplicateUsername(String userModelAttrName, String username, RealmModel realm, KeycloakSession session, UserModel user) {
        // 仅处理 USERNAME 属性映射
        if (UserModel.USERNAME.equalsIgnoreCase(userModelAttrName)) {
            if (username == null || username.isEmpty()) {
                throw new ModelException("Cannot set an empty username");
            }
            boolean usernameChanged = !username.equals(user.getUsername());
            if (realm.isEditUsernameAllowed() && usernameChanged) {
                UserModel that = session.users().getUserByUsername(realm, username);
                if (that != null && !that.getId().equals(user.getId())) {
                    throw new ModelDuplicateException(
                            String.format("Cannot change the username to '%s' because the username already exists in keycloak", username),
                            UserModel.USERNAME);
                }
            } else if (usernameChanged) {
                if (realm.isRegistrationEmailAsUsername() && username.equals(user.getEmail())) {
                    return;
                }
                throw new ModelException("Cannot change username if the realm is not configured to allow edit the usernames");
            }
        }
    }

    /** 代理用户模型：可写模式下将属性变更同步至 LDAP，并支持始终从 LDAP 读取。 */
    @Override
    public UserModel proxy(final LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        final String userModelAttrName = getUserModelAttribute();
        final String ldapAttrName = getLdapAttributeName();
        boolean isAlwaysReadValueFromLDAP = parseBooleanParameter(mapperModel, ALWAYS_READ_VALUE_FROM_LDAP);
        final boolean isMandatoryInLdap = parseBooleanParameter(mapperModel, IS_MANDATORY_IN_LDAP);
        final boolean decodeAsUuid = shouldDecodeAsUuid();
        final boolean isBinaryAttribute = parseBooleanParameter(mapperModel, IS_BINARY_ATTRIBUTE);
        final String attributeDefaultValue = getAttributeDefaultValue();

        // 可写模式：属性写入同时传播到 LDAP
        if (ldapProvider.getEditMode() == UserStorageProvider.EditMode.WRITABLE && !isReadOnly()) {

            delegate = new TxAwareLDAPUserModelDelegate(delegate, ldapProvider, ldapUser) {

                @Override
                public void setSingleAttribute(String name, String value) {
                    if (UserModel.USERNAME.equals(name)) {
                        setUsername(value);
                    } else if (UserModel.EMAIL.equals(name)) {
                        setEmail(value);
                    } else if (setLDAPAttribute(name, value)) {
                        super.setSingleAttribute(name, value);
                    }
                }

                @Override
                public void setAttribute(String name, List<String> values) {
                    if (UserModel.USERNAME.equals(name)) {
                        setUsername((values != null && values.size() > 0) ? values.get(0) : null);
                    } else if (UserModel.EMAIL.equals(name)) {
                        setEmail((values != null && values.size() > 0) ? values.get(0) : null);
                    } else if (setLDAPAttribute(name, values)) {
                        super.setAttribute(name, values);
                    }
                }

                @Override
                public void removeAttribute(String name) {
                    if(!UserModel.USERNAME.equals(name)){
                        // 不允许移除用户名
                        if (setLDAPAttribute(name, null)) {
                            super.removeAttribute(name);
                        }
                    }
                }

                @Override
                public void setUsername(String username) {
                    String lowercaseUsername = KeycloakModelUtils.toLowerCaseSafe(username);
                    checkDuplicateUsername(userModelAttrName, lowercaseUsername, realm, ldapProvider.getSession(), this);
                    setLDAPAttribute(UserModel.USERNAME, lowercaseUsername);
                    super.setUsername(lowercaseUsername);
                }

                @Override
                public void setEmail(String email) {
                    String lowercaseEmail = KeycloakModelUtils.toLowerCaseSafe(email);
                    checkDuplicateEmail(userModelAttrName, email, realm, ldapProvider.getSession(), this);

                    setLDAPAttribute(UserModel.EMAIL, email);
                    super.setEmail(lowercaseEmail);
                }

                @Override
                public void setEnabled(boolean enabled) {
                    setLDAPAttribute(UserModel.ENABLED, Boolean.toString(enabled));
                    super.setEnabled(enabled);
                }

                @Override
                public void setLastName(String lastName) {
                    setLDAPAttribute(UserModel.LAST_NAME, lastName);
                    super.setLastName(lastName);
                }

                @Override
                public void setFirstName(String firstName) {
                    setLDAPAttribute(UserModel.FIRST_NAME, firstName);
                    super.setFirstName(firstName);
                }

                @Override
                public void setEmailVerified(boolean verified) {
                    setLDAPAttribute(UserModel.EMAIL_VERIFIED, Boolean.toString(verified));
                    super.setEmailVerified(verified);
                }

                @Override
                public String getUsername() {
                    if (UserModel.USERNAME.equals(userModelAttrName)) {
                        return ofNullable(ldapUser.getAttributeAsString(ldapAttrName))
                                .map(this::toLowerCaseIfImportEnabled)
                                .orElse(null);
                    }
                    return super.getUsername();
                }

                @Override
                public String getEmail() {
                    if (UserModel.EMAIL.equals(userModelAttrName)) {
                        return ofNullable(ldapUser.getAttributeAsString(ldapAttrName))
                                .map(this::toLowerCaseIfImportEnabled)
                                .orElse(null);
                    }
                    return super.getEmail();
                }

                protected boolean setLDAPAttribute(String modelAttrName, Object value) {
                    if (modelAttrName.equalsIgnoreCase(userModelAttrName)) {
                        if (UserAttributeLDAPStorageMapper.logger.isTraceEnabled()) {
                            UserAttributeLDAPStorageMapper.logger.tracef("Pushing user attribute to LDAP. username: %s, Model attribute name: %s, LDAP attribute name: %s, Attribute value: %s", getUsername(), modelAttrName, ldapAttrName, value);
                        }

                        markUpdatedAttributeInTransaction(modelAttrName);

                        if (value == null) {
                            if (isMandatoryInLdap && attributeDefaultValue != null) {
                                ldapUser.setSingleAttribute(ldapAttrName, attributeDefaultValue);
                            } else {
                                ldapUser.setAttribute(ldapAttrName, new LinkedHashSet<String>());
                            }
                        } else if (value instanceof String) {
                            ldapUser.setSingleAttribute(ldapAttrName, (String) value);
                        } else {
                            List<String> asList = (List<String>) value;
                            if (asList.isEmpty() && isMandatoryInLdap && attributeDefaultValue != null) {
                                ldapUser.setSingleAttribute(ldapAttrName, attributeDefaultValue);
                            } else {
                                ldapUser.setAttribute(ldapAttrName, new LinkedHashSet<>(asList));
                            }
                        }

                        if (isBinaryAttribute) {
                            UserAttributeLDAPStorageMapper.logger.debugf("Skip writing model attribute '%s' to DB for user '%s' as it is mapped to binary LDAP attribute.", userModelAttrName, getUsername());
                            return false;
                        } else {
                            return true;
                        }
                    }

                    return true;
                }

                private String toLowerCaseIfImportEnabled(String value) {
                    if (getLdapProvider().getModel().isImportEnabled()) {
                        return value.toLowerCase();
                    }
                    return value;
                }
            };

        } else if (isBinaryAttribute) {

            delegate = new UserModelDelegate(delegate) {

                @Override
                public void setSingleAttribute(String name, String value) {
                    if (name.equalsIgnoreCase(userModelAttrName)) {
                        logSkipDBWrite();
                    } else {
                        super.setSingleAttribute(name, value);
                    }
                }

                @Override
                public void setAttribute(String name, List<String> values) {
                    if (name.equalsIgnoreCase(userModelAttrName)) {
                        logSkipDBWrite();
                    } else {
                        super.setAttribute(name, values);
                    }
                }

                @Override
                public void removeAttribute(String name) {
                    if (name.equalsIgnoreCase(userModelAttrName)) {
                        logSkipDBWrite();
                    } else {
                        super.removeAttribute(name);
                    }
                }

                private void logSkipDBWrite() {
                    logger.debugf("Skip writing model attribute '%s' to DB for user '%s' as it is mapped to binary LDAP attribute", userModelAttrName, getUsername());
                }

            };

        }

        // 优先从 LDAP 读取属性值，而非本地 Keycloak 数据库
        if (isAlwaysReadValueFromLDAP) {

            delegate = new UserModelDelegate(delegate) {

                private String decodeValue(String value) {
                    if (decodeAsUuid) {
                        LDAPConfig ldapConfig = ldapProvider.getLdapIdentityStore().getConfig();
                        return LDAPUtil.decodeBase64ToUuid(value, ldapConfig);
                    }
                    return value;
                }

                @Override
                public String getFirstAttribute(String name) {
                    if (name.equalsIgnoreCase(userModelAttrName)) {
                        return decodeValue(ldapUser.getAttributeAsString(ldapAttrName));
                    } else {
                        return super.getFirstAttribute(name);
                    }
                }

                @Override
                public Stream<String> getAttributeStream(String name) {
                    if (name.equalsIgnoreCase(userModelAttrName)) {
                        Collection<String> ldapAttrValue = ldapUser.getAttributeAsSet(ldapAttrName);
                        if (ldapAttrValue == null) {
                            return Stream.empty();
                        } else {
                            return ldapAttrValue.stream().map(this::decodeValue);
                        }
                    } else {
                        return super.getAttributeStream(name);
                    }
                }

                @Override
                public Map<String, List<String>> getAttributes() {
                    Map<String, List<String>> attrs = new HashMap<>(super.getAttributes());

                    Set<String> allLdapAttrValues = ldapUser.getAttributeAsSet(ldapAttrName);
                    if (allLdapAttrValues != null) {
                        attrs.put(userModelAttrName, allLdapAttrValues.stream()
                                .map(this::decodeValue).collect(Collectors.toList()));
                    } else {
                        attrs.remove(userModelAttrName);
                    }
                    return attrs;
                }

                @Override
                public String getEmail() {
                    if (UserModel.EMAIL.equalsIgnoreCase(userModelAttrName)) {
                        return ldapUser.getAttributeAsString(ldapAttrName);
                    } else {
                        return super.getEmail();
                    }
                }

                @Override
                public boolean isEnabled() {
                    if (UserModel.ENABLED.equalsIgnoreCase(userModelAttrName)) {
                        return Boolean.parseBoolean(ldapUser.getAttributeAsString(ldapAttrName));
                    } else {
                        return super.isEnabled();
                    }
                }

                @Override
                public boolean isEmailVerified() {
                    if (UserModel.EMAIL_VERIFIED.equalsIgnoreCase(userModelAttrName)) {
                        return Boolean.parseBoolean(ldapUser.getAttributeAsString(ldapAttrName));
                    } else {
                        return super.isEmailVerified();
                    }
                }

                @Override
                public String getLastName() {
                    if (UserModel.LAST_NAME.equalsIgnoreCase(userModelAttrName)) {
                        return ldapUser.getAttributeAsString(ldapAttrName);
                    } else {
                        return super.getLastName();
                    }
                }

                @Override
                public String getFirstName() {
                    if (UserModel.FIRST_NAME.equalsIgnoreCase(userModelAttrName)) {
                        return ldapUser.getAttributeAsString(ldapAttrName);
                    } else {
                        return super.getFirstName();
                    }
                }

            };
        }

        return delegate;
    }

    /** 查询前将用户模型条件转换为 LDAP 属性并加入返回字段。 */
    @Override
    public void beforeLDAPQuery(LDAPQuery query) {
        String userModelAttrName = getUserModelAttribute();
        String ldapAttrName = getLdapAttributeName();

        // 将映射属性加入 LDAP 查询返回字段
        query.addReturningLdapAttribute(ldapAttrName);
        if (isReadOnly()) {
            query.addReturningReadOnlyLdapAttribute(ldapAttrName);
        }

        // 将查询条件中的用户模型属性名替换为 LDAP 属性名
        for (Condition condition : query.getConditions()) {
            condition.updateParameterName(userModelAttrName, ldapAttrName);
            String parameterName = condition.getParameterName();
            if (parameterName != null && (parameterName.equalsIgnoreCase(userModelAttrName) || parameterName.equalsIgnoreCase(ldapAttrName))) {
                condition.setBinary(isBinaryAttribute());
            }
        }
    }

    private String getAttributeDefaultValue() {
        String attributeDefaultValue = mapperModel.getConfig().getFirst(ATTRIBUTE_DEFAULT_VALUE);
        attributeDefaultValue = attributeDefaultValue == null || attributeDefaultValue.trim().isEmpty()? null : attributeDefaultValue;
        boolean forceDefault = mapperModel.get(FORCE_DEFAULT_VALUE, true);
        return forceDefault && attributeDefaultValue == null? LDAPConstants.EMPTY_ATTRIBUTE_VALUE : attributeDefaultValue;
    }

    private String getUserModelAttribute() {
        return mapperModel.getConfig().getFirst(USER_MODEL_ATTRIBUTE);
    }

    String getLdapAttributeName() {
        return mapperModel.getConfig().getFirst(LDAP_ATTRIBUTE);
    }

    private boolean isBinaryAttribute() {
        return mapperModel.get(IS_BINARY_ATTRIBUTE, false);
    }

    private boolean shouldDecodeAsUuid() {
        if (!isBinaryAttribute()) return false;
        String decoder = mapperModel.getConfig().getFirst(BINARY_ATTRIBUTE_DECODER);
        if (BINARY_DECODER_BASE64.equals(decoder)) return false;
        if (BINARY_DECODER_UUID.equals(decoder)) return true;
        // "auto" 或未设置：当 LDAP 属性与配置的 UUID 属性一致时按 UUID 解码
        LDAPConfig ldapConfig = ldapProvider.getLdapIdentityStore().getConfig();
        return getLdapAttributeName().equalsIgnoreCase(ldapConfig.getUuidLDAPAttributeName());
    }

    private boolean isReadOnly() {
        return parseBooleanParameter(mapperModel, READ_ONLY);
    }

    /** 将 LDAP 字符串值写入 UserModel 的 Java 属性（String/Boolean）。 */
    protected void setPropertyOnUserModel(Property<Object> userModelProperty, UserModel user, String ldapAttrValue) {
        if (ldapAttrValue == null) {
            userModelProperty.setValue(user, null);
        } else {
            Class<Object> clazz = userModelProperty.getJavaClass();
            Object currentValue = userModelProperty.getValue(user);

            if (String.class.equals(clazz)) {
                if (ldapAttrValue.equals(currentValue)) {
                    return;
                }
                userModelProperty.setValue(user, ldapAttrValue);
            } else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
                Boolean boolVal = Boolean.valueOf(ldapAttrValue);
                if (boolVal.equals(currentValue)) {
                    return;
                }
                userModelProperty.setValue(user, boolVal);
            } else {
                logger.warnf("Don't know how to set the property '%s' on user '%s' . Value of LDAP attribute is '%s' ", userModelProperty.getName(), user.getUsername(), ldapAttrValue.toString());
            }
        }
    }
}
