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

package org.keycloak.broker.oidc.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.saml.mappers.UsernameTemplateMapper.Target;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.social.bitbucket.BitbucketIdentityProviderFactory;
import org.keycloak.social.facebook.FacebookIdentityProviderFactory;
import org.keycloak.social.github.GitHubIdentityProviderFactory;
import org.keycloak.social.gitlab.GitLabIdentityProviderFactory;
import org.keycloak.social.google.GoogleIdentityProviderFactory;
import org.keycloak.social.instagram.InstagramIdentityProviderFactory;
import org.keycloak.social.linkedin.LinkedInOIDCIdentityProviderFactory;
import org.keycloak.social.microsoft.MicrosoftIdentityProviderFactory;
import org.keycloak.social.openshift.OpenshiftV4IdentityProviderFactory;
import org.keycloak.social.paypal.PayPalIdentityProviderFactory;
import org.keycloak.social.stackoverflow.StackoverflowIdentityProviderFactory;
import org.keycloak.social.twitter.TwitterIdentityProviderFactory;

import org.jboss.logging.Logger;

import static org.keycloak.broker.saml.mappers.UsernameTemplateMapper.TARGET;
import static org.keycloak.broker.saml.mappers.UsernameTemplateMapper.TARGETS;
import static org.keycloak.broker.saml.mappers.UsernameTemplateMapper.TRANSFORMERS;
import static org.keycloak.broker.saml.mappers.UsernameTemplateMapper.getTarget;

/**
 * 用户名模板映射器：用 ${ALIAS}、${CLAIM.*} 等占位符格式化导入用户名。
 * <p>支持 LOCAL/BROKER_ID/BROKER_USERNAME 三种写入目标。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UsernameTemplateMapper extends AbstractClaimMapper {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(UsernameTemplateMapper.class);

    public static final String[] COMPATIBLE_PROVIDERS = {
            KeycloakOIDCIdentityProviderFactory.PROVIDER_ID,
            OIDCIdentityProviderFactory.PROVIDER_ID,
            BitbucketIdentityProviderFactory.PROVIDER_ID,
            FacebookIdentityProviderFactory.PROVIDER_ID,
            GitHubIdentityProviderFactory.PROVIDER_ID,
            GitLabIdentityProviderFactory.PROVIDER_ID,
            GoogleIdentityProviderFactory.PROVIDER_ID,
            InstagramIdentityProviderFactory.PROVIDER_ID,
            LinkedInOIDCIdentityProviderFactory.PROVIDER_ID,
            MicrosoftIdentityProviderFactory.PROVIDER_ID,
            OpenshiftV4IdentityProviderFactory.PROVIDER_ID,
            PayPalIdentityProviderFactory.PROVIDER_ID,
            StackoverflowIdentityProviderFactory.PROVIDER_ID,
            TwitterIdentityProviderFactory.PROVIDER_ID
    };

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
    private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

    /** 配置键：用户名模板字符串。 */
    public static final String TEMPLATE = "template";

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(TEMPLATE);
        property.setLabel("Template");
        property.setHelpText("Template to use to format the username to import.  Substitutions are enclosed in ${}.  For example: '${ALIAS}.${CLAIM.sub}'.  ALIAS is the provider alias.  CLAIM.<NAME> references an ID or Access token claim. \n"
          + "The substitution can be converted to upper or lower case by appending |uppercase or |lowercase to the substituted value, e.g. '${CLAIM.sub | lowercase}");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setDefaultValue("${ALIAS}.${CLAIM.preferred_username}");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(TARGET);
        property.setLabel("Target");
        property.setHelpText("Destination field for the mapper. LOCAL (default) means that the changes are applied to the username stored in local database upon user import. BROKER_ID and BROKER_USERNAME means that the changes are stored into the ID or username used for federation user lookup, respectively.");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(TARGETS);
        property.setDefaultValue(Target.LOCAL.toString());
        configProperties.add(property);
    }

    /** 映射器 provider id。 */
    public static final String PROVIDER_ID = "oidc-username-idp-mapper";

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

    /** @return 控制台显示类型 Username Template Importer */
    @Override
    public String getDisplayType() {
        return "Username Template Importer";
    }

    /** 旧版同步：无操作。 */
    @Override
    public void updateBrokeredUserLegacy(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
    }

    /** 目标为 LOCAL 且未启用邮箱作用户名时，将格式化用户名写入本地用户。 */
    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        // preprocessFederatedIdentity 已处理模板，此处仅在需要时更新本地用户名
        // 启用“邮箱作用户名”时不覆盖 username
        if (getTarget(mapperModel.getConfig().get(TARGET)) == Target.LOCAL && !realm.isRegistrationEmailAsUsername()) {
            user.setUsername(context.getModelUsername());
        }
    }

    /** 模板占位符正则：${VAR} 或 ${VAR | uppercase/lowercase}。 */
    private static final Pattern SUBSTITUTION = Pattern.compile("\\$\\{([^}]+?)(?:\\s*\\|\\s*(\\S+)\\s*)?\\}");

    /** 预处理：按模板解析并设置用户名。 */
    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        setUserNameFromTemplate(mapperModel, context);
    }

    /** 解析模板占位符并写入 {@link Target} 指定字段。 */
    private void setUserNameFromTemplate(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
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
            } else if (variable.startsWith("CLAIM.")) {
                String name = variable.substring("CLAIM.".length());
                Object value = AbstractClaimMapper.getClaimValue(context, name);
                if (value == null) {
                    hasUnresolvedVariable = true;
                    m.appendReplacement(sb, "");
                } else {
                    if (value instanceof Collection && ((Collection<?>) value).size() == 1) {
                        // 单元素集合取首项，避免 toString 产生 "[foo]" 形式
                        value = ((Collection<?>) value).iterator().next();
                    }
                    m.appendReplacement(sb, transformer.apply(value.toString()));
                }
            } else {
                m.appendReplacement(sb, m.group(1));
            }

        }
        m.appendTail(sb);

        if (hasUnresolvedVariable) {
            logger.warnf("Username template '%s' for identity provider '%s' contains unresolved claims. Check that the identity provider is sending the expected claims.",
                    template, context.getIdpConfig().getAlias());
        }

        Target t = getTarget(mapperModel.getConfig().get(TARGET));
        t.set(context, hasUnresolvedVariable ? "" : sb.toString());
    }

    /** @return 按模板格式化待导入用户名 */
    @Override
    public String getHelpText() {
        return "Format the username to import.";
    }
}
