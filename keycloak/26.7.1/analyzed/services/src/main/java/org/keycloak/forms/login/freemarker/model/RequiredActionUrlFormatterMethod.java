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

import java.net.URI;
import java.util.List;

import org.keycloak.models.RealmModel;
import org.keycloak.services.Urls;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker 模板方法：按 Realm 与相对路径格式化必需操作（Required Action）URL。
 * <p>实现 {@link TemplateMethodModelEx}，供 FTL 调用 {@code requiredActionUrl(action, path)}。</p>
 */
public class RequiredActionUrlFormatterMethod implements TemplateMethodModelEx {
    /** Realm 名称。 */
    private final String realm;
    /** 服务基础 URI。 */
    private final URI baseUri;

    /** @param realm Realm 模型 @param baseUri 基础 URI */
    public RequiredActionUrlFormatterMethod(RealmModel realm, URI baseUri) {
        this.realm = realm.getName();
        this.baseUri = baseUri;
    }

    /** @param list 参数列表：[action, relativePath] @return 完整必需操作 URL */
    @Override
    public Object exec(List list) throws TemplateModelException {
        String action = list.get(0).toString();
        String relativePath = list.get(1).toString();
        String url = Urls.requiredActionBase(baseUri).path(relativePath).build(realm, action).toString();
        return url;
    }
}
