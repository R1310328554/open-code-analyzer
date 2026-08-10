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

package org.keycloak.representations;

import org.keycloak.common.Version;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Keycloak 服务器版本与构建时间的 JSON 表示，供 REST 端点返回当前运行版本信息。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class VersionRepresentation {
    /** 单例实例，字段值在类加载时从 {@link Version} 初始化。 */
    public static final VersionRepresentation SINGLETON;

    /** 当前 Keycloak 版本号字符串。 */
    private final String version = Version.VERSION;
    /** 构建时间戳字符串。 */
    private final String buildTime = Version.BUILD_TIME;

    static {
         SINGLETON = new VersionRepresentation();
    }

    /** @return 版本号，JSON 字段名为 {@code version} */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /** @return 构建时间，JSON 字段名为 {@code build-time} */
    @JsonProperty("build-time")
    public String getBuildTime() {
        return buildTime;
    }
}
