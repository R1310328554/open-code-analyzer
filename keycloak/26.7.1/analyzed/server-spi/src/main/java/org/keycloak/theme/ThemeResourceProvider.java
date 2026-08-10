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

package org.keycloak.theme;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.keycloak.provider.Provider;

/**
 * 主题资源提供者：加载额外的模板与静态资源。例如自定义认证器需要额外模板或 JavaScript 文件时可使用。
 * 查找顺序为先主题、后主题资源提供者；主题中的同名资源可覆盖提供者中的资源。
 *
 * A theme resource provider can be used to load additional templates and resources. An example use of this would be
 * a custom authenticator that requires an additional template and a JavaScript file.
 *
 * The theme is searched for templates and resources first. Theme resource providers are only searched if the template
 * or resource is not found. This allows overriding templates and resources from theme resource providers in the theme.
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface ThemeResourceProvider extends Provider {

    /**
     * 按名称加载模板。
     * Load the template for the specific name
     *
     * @param name the template name
     * @return the URL of the template, or null if the template is unknown
     * @throws IOException
     */
    URL getTemplate(String name) throws IOException;

    /**
     * 按路径加载资源流。
     * Load the resource for the specific path
     *
     * @param path the resource path
     * @return an InputStream to read the resource, or null if the resource is unknown
     * @throws IOException
     */
    InputStream getResourceAsStream(String path) throws IOException;

    /**
     * 按 bundle 基名与区域设置加载消息包。
     * Load the message bundle for the specific name and locale
     * 
     * @param baseBundlename The base name of the bundle, such as "messages" in
     * messages_en.properties.
     * @param locale The locale of the desired message bundle.
     * @return The localized messages from the bundle.
     * @throws IOException If bundle can not be read.
     */
    default Properties getMessages(String baseBundlename, Locale locale) throws IOException{
        return new Properties();
    }

}
