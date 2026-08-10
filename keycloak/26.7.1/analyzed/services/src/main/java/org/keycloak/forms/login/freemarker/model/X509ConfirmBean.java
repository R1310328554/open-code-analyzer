/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

import jakarta.ws.rs.core.MultivaluedMap;

/**
 * X509 客户端证书确认页 FreeMarker Bean：将 POST 表单数据转为单值 Map 供模板回显。
 * <p>用于 X509 认证流程中用户确认证书信息的表单页面。</p>
 *
 * @author vramik
 */
public class X509ConfirmBean {

    /** 表单字段名 → 首值 映射，供模板遍历展示。 */
    private Map<String, String> formData;

    /** @param formData HTTP 表单参数；null 时初始化为空 Map */
    public X509ConfirmBean(MultivaluedMap<String, String> formData) {
        this.formData = new HashMap<>();

        if (formData != null) {
            formData.keySet().stream().forEach((key) -> this.formData.put(key, formData.getFirst(key)));
        }
    }

    /** @return 单值化后的表单数据 */
    public Map<String, String> getFormData() {
        return formData;
    }

}
