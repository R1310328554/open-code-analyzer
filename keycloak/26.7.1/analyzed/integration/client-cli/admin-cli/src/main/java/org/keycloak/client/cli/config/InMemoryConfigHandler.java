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
package org.keycloak.client.cli.config;

/**
 * 内存中的 {@link ConfigHandler} 实现，主要用于测试。
 * <p>
 * 配置数据保存在 JVM 堆内，不写入磁盘。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class InMemoryConfigHandler implements ConfigHandler {

    /** 缓存的配置数据。 */
    private ConfigData cached;

    /** 对缓存配置应用更新操作。 */
    @Override
    public void saveMergeConfig(ConfigUpdateOperation config) {
        config.update(cached);
    }

    /** 返回当前缓存的配置。 */
    @Override
    public ConfigData loadConfig() {
        return cached;
    }

    /** 注入或替换内存中的配置数据。 */
    public void setConfigData(ConfigData data) {
        this.cached = data;
    }
}
