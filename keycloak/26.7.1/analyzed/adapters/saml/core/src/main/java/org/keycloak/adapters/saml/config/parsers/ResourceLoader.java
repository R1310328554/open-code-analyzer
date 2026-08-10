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

package org.keycloak.adapters.saml.config.parsers;

import java.io.InputStream;

/**
 * 从类路径或文件系统加载 SAML 适配器配置资源的回调接口。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ResourceLoader {
    /**
     * 按资源路径打开输入流。
     *
     * @param resource 资源标识（如 classpath 路径）
     * @return 可读的资源流，不存在时由实现决定返回 null 或抛异常
     */
    InputStream getResourceAsStream(String resource);
}
