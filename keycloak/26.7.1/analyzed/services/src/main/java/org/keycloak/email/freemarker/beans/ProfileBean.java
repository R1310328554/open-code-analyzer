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
package org.keycloak.email.freemarker.beans;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.forms.login.freemarker.model.OrganizationBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.userprofile.UserProfileProvider;

import org.jboss.logging.Logger;

/**
 * 用户资料 FreeMarker Bean：将 {@link UserModel} 属性暴露为邮件模板变量。
 * <p>按用户配置规范过滤多值属性，并懒加载用户所属组织列表。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ProfileBean {

    private static final Logger logger = Logger.getLogger(ProfileBean.class);

    /** 目标用户模型。 */
    private final UserModel user;
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 单值化后的用户自定义属性映射。 */
    private final Map<String, String> attributes = new HashMap<>();
    /** 用户所属组织 Bean 列表（懒加载）。 */
    private List<OrganizationBean> organizations;

    /** @param user 目标用户 @param session 当前会话 */
    public ProfileBean(UserModel user, KeycloakSession session) {
        this.user = user;
        this.session = session;

        if (user.getAttributes() != null) {
            // TODO：属性本可返回多值，但变更会破坏现有邮件模板契约，故仅保留首个值
            UserProfileProvider provider = session.getProvider(UserProfileProvider.class);
            UPConfig configuration = provider.getConfiguration();

            for (Map.Entry<String, List<String>> attr : user.getAttributes().entrySet()) {
                List<String> attrValue = attr.getValue();
                if (attrValue != null && attrValue.size() > 0) {
                    attributes.put(attr.getKey(), attrValue.get(0));
                }

                UPAttribute attribute = configuration.getAttribute(attr.getKey());
                boolean multivalued = attribute != null && attribute.isMultivalued();

                if (!multivalued && attrValue != null && attrValue.size() > 1) {
                    logger.warnf("There are more values for attribute '%s' of user '%s' . Will display just first value", attr.getKey(), user.getUsername());
                }
            }
        }
    }

    /** @return 用户名 */
    public String getUsername() { return user.getUsername(); }

    /** @return 名 */
    public String getFirstName() {
        return user.getFirstName();
    }

    /** @return 姓 */
    public String getLastName() {
        return user.getLastName();
    }

    /** @return 邮箱地址 */
    public String getEmail() {
        return user.getEmail();
    }

    /** @return 单值化后的自定义属性映射 */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /** @return 用户所属组织 Bean 列表（组织功能未启用时为空） */
    public List<OrganizationBean> getOrganizations() {
        if (organizations == null) {
            if (!Organizations.isEnabled(session)) {
                organizations = Collections.emptyList();
            } else {
                organizations = Organizations.getProvider(session).getByMember(user)
                        .map(o -> new OrganizationBean(o, user))
                        .toList();
            }
        }
        return organizations;
    }
}
