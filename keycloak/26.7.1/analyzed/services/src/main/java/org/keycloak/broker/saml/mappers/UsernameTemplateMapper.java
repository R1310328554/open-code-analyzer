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

package org.keycloak.broker.saml.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.saml.SAMLEndpoint;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.assertion.SubjectType;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

/**
 * SAML 用户名模板映射器：用 ${} 占位符格式化导入用户名。
 * <p>支持 ALIAS、NAMEID、ATTRIBUTE.* 替换及 uppercase/lowercase/localpart 转换。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UsernameTemplateMapper extends AbstractIdentityProviderMapper {

    private static final Logger logger = Logger.getLogger(UsernameTemplateMapper.class);

    public static final String[] COMPATIBLE_PROVIDERS = {SAMLIdentityProviderFactory.PROVIDER_ID};

    /** 配置键：用户名模板字符串。 */
    public static final String TEMPLATE = "template";
    /** 配置键：模板结果写入目标（LOCAL/BROKER_ID/BROKER_USERNAME）。 */
    public static final String TARGET = "target";

    /** 模板格式化结果的目标字段。 */
    public enum Target  {
        /** 写入本地数据库用户名。 */
        LOCAL              { public void set(BrokeredIdentityContext context, String value) { context.setModelUsername(value); } },
        /** 写入联邦查找用的 broker ID。 */
        BROKER_ID          { public void set(BrokeredIdentityContext context, String value) { context.setId(value); } },
        /** 写入联邦查找用的 broker 用户名。 */
        BROKER_USERNAME    { public void set(BrokeredIdentityContext context, String value) { context.setUsername(value); } };
        public abstract void set(BrokeredIdentityContext context, String value);
    }
    public static final List<String> TARGETS = Arrays.asList(Target.LOCAL.toString(), Target.BROKER_ID.toString(), Target.BROKER_USERNAME.toString());

    public static final Map<String, UnaryOperator<String>> TRANSFORMERS = new HashMap<>();

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
    private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(TEMPLATE);
        property.setLabel("Template");
        property.setHelpText("Template to use to format the username to import.  Substitutions are enclosed in ${}.  For example: '${ALIAS}.${NAMEID}'.  ALIAS is the provider alias.  NAMEID is that SAML name id assertion.  ATTRIBUTE.<NAME> references a SAML attribute where name is the attribute name or friendly name. \n"
          + "The substitution can be converted to upper or lower case by appending |uppercase or |lowercase to the substituted value, e.g. '${NAMEID | lowercase} \n"
          + "Local part of email can be extracted by appending |localpart to the substituted value, e.g. ${ATTRIBUTE.email | localpart}. If \"@\" is not part of the string, this conversion leaves the substitution untouched.");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue("${ALIAS}.${NAMEID}");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(TARGET);
        property.setLabel("Target");
        property.setHelpText("Destination field for the mapper. LOCAL (default) means that the changes are applied to the username stored in local database upon user import. BROKER_ID and BROKER_USERNAME means that the changes are stored into the ID or username used for federation user lookup, respectively.");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(TARGETS);
        property.setDefaultValue(Target.LOCAL.toString());
        configProperties.add(property);

        TRANSFORMERS.put("uppercase", String::toUpperCase);
        TRANSFORMERS.put("lowercase", String::toLowerCase);
        TRANSFORMERS.put("localpart", UsernameTemplateMapper::getEmailLocalPart);
    }

    /** 映射器 provider id。 */
    public static final String PROVIDER_ID = "saml-username-idp-mapper";

    /** 提取邮箱 @ 前的本地部分；无 @ 时原样返回。 */
    public static String getEmailLocalPart(String email) {
        int index = email == null ? -1 : email.lastIndexOf('@');
        if (index >= 0) {
            return email.substring(0, index);
        } else {
            return email;
        }
    }

    @Override
    public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
        return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getDisplayCategory() {
        return "Preprocessor";
    }

    @Override
    public String getDisplayType() {
        return "Username Template Importer";
    }

    @Override
    public void updateBrokeredUserLegacy(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
    }

    /** TARGET=LOCAL 且未启用 email-as-username 时同步本地用户名。 */
    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        // preprocessFederatedIdentity 已处理模板；仅在非 email-as-username 时更新本地用户名
        if (getTarget(mapperModel.getConfig().get(TARGET)) == Target.LOCAL && !realm.isRegistrationEmailAsUsername()) {
            user.setUsername(context.getModelUsername());
        }
    }

    private static final Pattern SUBSTITUTION = Pattern.compile("\\$\\{([^}]+?)(?:\\s*\\|\\s*(\\S+)\\s*)?\\}");

    /** 预处理：按模板格式化用户名并写入目标字段。 */
    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        setUserNameFromTemplate(mapperModel, context);
    }

    /** 解析 ${} 占位符（ALIAS/UUID/NAMEID/ATTRIBUTE.*）并应用转换器。 */
    private void setUserNameFromTemplate(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        AssertionType assertion = (AssertionType)context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);
        String template = mapperModel.getConfig().get(TEMPLATE);
        Matcher m = SUBSTITUTION.matcher(template);
        StringBuffer sb = new StringBuffer();
        boolean hasUnresolvedVariable = false;
        while (m.find()) {
            String variable = m.group(1);
            UnaryOperator<String> transformer = Optional.ofNullable(m.group(2)).map(TRANSFORMERS::get).orElse(UnaryOperator.identity());

            if (variable.equals("ALIAS")) {
                m.appendReplacement(sb, transformer.apply(context.getIdpConfig().getAlias()));
            } else if (variable.equals("UUID")) {
                m.appendReplacement(sb, transformer.apply(KeycloakModelUtils.generateId()));
            } else if (variable.equals("NAMEID")) {
                SubjectType subject = assertion.getSubject();
                SubjectType.STSubType subType = subject.getSubType();
                NameIDType subjectNameID = (NameIDType) subType.getBaseID();
                m.appendReplacement(sb, transformer.apply(subjectNameID.getValue()));
            } else if (variable.startsWith("ATTRIBUTE.")) {
                String name = variable.substring("ATTRIBUTE.".length());
                String value = null;
                for (AttributeStatementType statement : assertion.getAttributeStatements()) {
                    for (AttributeStatementType.ASTChoiceType choice : statement.getAttributes()) {
                        AttributeType attr = choice.getAttribute();
                        if (name.equals(attr.getName()) || name.equals(attr.getFriendlyName())) {
                            List<Object> attributeValue = attr.getAttributeValue();
                            if (attributeValue != null && !attributeValue.isEmpty()) {
                                value = attributeValue.get(0).toString();
                            }
                            break;
                        }
                    }
                }
                if (value == null) {
                    hasUnresolvedVariable = true;
                    m.appendReplacement(sb, "");
                } else {
                    m.appendReplacement(sb, transformer.apply(value));
                }
            } else {
                m.appendReplacement(sb, m.group(1));
            }

        }
        m.appendTail(sb);

        if (hasUnresolvedVariable) {
            logger.warnf("Username template '%s' for identity provider '%s' contains unresolved attributes. Check that the identity provider is sending the expected SAML attributes.",
                    template, context.getIdpConfig().getAlias());
        }

        Target t = getTarget(mapperModel.getConfig().get(TARGET));
        t.set(context, hasUnresolvedVariable ? "" : sb.toString());
    }

    /** @return 格式化待导入的用户名 */
    @Override
    public String getHelpText() {
        return "Format the username to import.";
    }

    /** 解析 TARGET 配置，无效或 null 时默认 LOCAL。 */
    public static Target getTarget(String value) {
        try {
            return value == null ? Target.LOCAL : Target.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return Target.LOCAL;
        }
    }

}
