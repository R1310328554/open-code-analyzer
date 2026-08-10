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
package org.keycloak.scripting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.ScriptEngine;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import org.jboss.logging.Logger;

/**
 * 默认 {@link ScriptingProviderFactory} 实现。
 * <p>创建 {@link DefaultScriptingProvider}，并可配置按 MIME 类型缓存 {@link ScriptEngine}（Nashorn 等线程安全引擎）。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class DefaultScriptingProviderFactory implements ScriptingProviderFactory {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(DefaultScriptingProviderFactory.class);

    /** 工厂标识 {@code default} */
    static final String ID = "default";

    /** 是否启用脚本引擎缓存 */
    private boolean enableScriptEngineCache;

    // 键为 MIME 类型；值为对应引擎。Nashorn 等可跨线程共享时才适合缓存
    /** MIME 类型 → 共享 ScriptEngine 缓存 */
    private Map<String, ScriptEngine> scriptEngineCache;

    /** 工厂配置作用域 */
    private Config.Scope config;

    /** 创建会话级脚本提供者 @param session Keycloak 会话 @return DefaultScriptingProvider */
    @Override
    public ScriptingProvider create(KeycloakSession session) {
        return new DefaultScriptingProvider(this);
    }

    /** 读取 enable-script-engine-cache 等配置 @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {
        this.config = config;
        this.enableScriptEngineCache = config.getBoolean("enable-script-engine-cache", true);
        logger.debugf("Enable script engine cache: %b", this.enableScriptEngineCache);
        if (enableScriptEngineCache) {
            scriptEngineCache = new ConcurrentHashMap<>();
        }
    }

    /** @return 是否启用脚本引擎缓存 */
    boolean isEnableScriptEngineCache() {
        return enableScriptEngineCache;
    }

    /** @return 脚本引擎缓存 Map（未启用缓存时可能为 null） */
    Map<String, ScriptEngine> getScriptEngineCache() {
        return scriptEngineCache;
    }

    /** @return 工厂配置作用域 */
    Config.Scope getConfig() {
        return config;
    }

    /** 工厂初始化后回调（无操作） @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        //NOOP
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {
        //NOOP
    }

    /** @return 工厂标识 {@link #ID} */
    @Override
    public String getId() {
        return ID;
    }

}
