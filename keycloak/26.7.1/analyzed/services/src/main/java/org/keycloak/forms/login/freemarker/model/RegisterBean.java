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
import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

/**
 * 用户注册 FreeMarker Bean：基于 {@link UserProfileContext#REGISTRATION} 渲染动态注册表单。
 * <p>保留 {@link #getFormData()} 以兼容依赖扁平 Map 的旧版模板。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class RegisterBean extends AbstractUserProfileBean {

    /** 扁平化表单数据副本（legacy 模板兼容）。 */
    private Map<String, String> formDataLegacy = new HashMap<>();

    /** @param formData 注册 POST 数据 @param session Keycloak 会话 */
    public RegisterBean(MultivaluedMap<String, String> formData, KeycloakSession session) {
        
        super(formData);
        init(session, true);
        
        if (formData != null) {
            for (String k : formData.keySet()) {
                this.formDataLegacy.put(k, formData.getFirst(k));
            }
        }
    }

    @Override
    /** 创建 {@link UserProfileContext#REGISTRATION} 场景的用户配置。 */
    protected UserProfile createUserProfile(UserProfileProvider provider) {
        return provider.create(UserProfileContext.REGISTRATION, null, (UserModel) null);
    }

    @Override
    /** 注册场景无预设默认值。 */
    protected Stream<String> getAttributeDefaultValues(String name) {
        return null;
    }
    
    @Override 
    /** @return 用户配置上下文名称（REGISTRATION） */
    public String getContext() {
        return UserProfileContext.REGISTRATION.name();
    }
    
    /** @return 扁平化表单字段映射（legacy） */
    public Map<String, String> getFormData() {
        return formDataLegacy;
    }

}
