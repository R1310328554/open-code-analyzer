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
package org.keycloak.forms.login.freemarker.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.authentication.requiredactions.util.UpdateProfileContext;

import org.jboss.logging.Logger;

/**
 * 用户资料 FreeMarker Bean：合并 {@link UpdateProfileContext} 与表单数据供资料编辑页回显。
 * <p>支持标准字段（username、email 等）及 user.attributes.* 自定义属性。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ProfileBean {

    private static final Logger logger = Logger.getLogger(ProfileBean.class);

    /** 待编辑的用户资料上下文。 */
    private UpdateProfileContext user;
    /** 表单 POST 数据（验证失败回显）。 */
    private MultivaluedMap<String, String> formData;

    /** 合并后的单值属性映射（供模板遍历）。 */
    private final Map<String, String> attributes = new HashMap<>();

    /** @param user 资料上下文 @param formData 表单回显数据 */
    public ProfileBean(UpdateProfileContext user, MultivaluedMap<String, String> formData) {
        this.user = user;
        this.formData = formData;

        Map<String, List<String>> modelAttrs = user.getAttributes();
        if (modelAttrs != null) {
            for (Map.Entry<String, List<String>> attr : modelAttrs.entrySet()) {
                List<String> attrValue = attr.getValue();
                if (attrValue != null && attrValue.size() > 0) {
                    attributes.put(attr.getKey(), attrValue.get(0));
                }

                if (attrValue != null && attrValue.size() > 1) {
                    logger.warnf("There are more values for attribute '%s' of user '%s' . Will display just first value", attr.getKey(), user.getUsername());
                }
            }
        }
        if (formData != null) {
            for (String key : formData.keySet()) {
                if (key.startsWith("user.attributes.")) {
                    String attribute = key.substring("user.attributes.".length());
                    attributes.put(attribute, formData.getFirst(key));
                }
            }
        }

    }

    /** @return Realm 是否允许编辑用户名 */
    public boolean isEditUsernameAllowed() {
        return user.isEditUsernameAllowed();
    }

    /** @return 是否允许编辑邮箱 */
    public boolean isEditEmailAllowed() {
        return user.isEditEmailAllowed();
    }

    /** @return 用户名（优先表单值） */
    public String getUsername() { return formData != null ? formData.getFirst("username") : user.getUsername(); }

    /** @return 名（优先表单值） */
    public String getFirstName() {
        return formData != null ? formData.getFirst("firstName") : user.getFirstName();
    }

    /** @return 姓（优先表单值） */
    public String getLastName() {
        return formData != null ? formData.getFirst("lastName") : user.getLastName();
    }

    /** @return 邮箱（优先表单值） */
    public String getEmail() {
        return formData != null ? formData.getFirst("email") : user.getEmail();
    }

    /** @return 合并后的自定义属性映射 */
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
