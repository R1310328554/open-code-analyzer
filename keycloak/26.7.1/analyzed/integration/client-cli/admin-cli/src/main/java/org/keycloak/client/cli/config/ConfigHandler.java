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
 * CLI 配置持久化处理器接口。
 * <p>
 * 抽象配置的加载与“读-改-写”合并保存，由 {@link FileConfigHandler} 与
 * {@link InMemoryConfigHandler} 分别实现文件与内存存储。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public interface ConfigHandler {

    /** 加载配置、应用 {@link ConfigUpdateOperation} 更新并持久化。 */
    void saveMergeConfig(ConfigUpdateOperation op);

    /** 加载当前配置快照。 */
    ConfigData loadConfig();

}
