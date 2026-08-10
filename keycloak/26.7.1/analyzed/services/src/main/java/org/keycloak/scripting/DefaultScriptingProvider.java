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

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.keycloak.models.ScriptModel;
import org.keycloak.services.ServicesLogger;

import org.jboss.logging.Logger;

/**
 * 默认脚本提供者实现。
 * <p>基于 {@link ScriptEngineManager} 按 MIME 类型获取 {@link ScriptEngine}，支持编译缓存与可调用/可求值脚本适配器。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class DefaultScriptingProvider implements ScriptingProvider {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(DefaultScriptingProvider.class);

    /** 所属工厂（提供引擎缓存配置） */
    private final DefaultScriptingProviderFactory factory;

    /** @param factory 脚本提供者工厂 */
    DefaultScriptingProvider(DefaultScriptingProviderFactory factory) {
        this.factory = factory;
    }

    /**
     * 将 {@link ScriptModel} 包装为可调用脚本适配器。
     * @param scriptModel        must not be {@literal null}
     * @param bindingsConfigurer must not be {@literal null}
     * @return {@link InvocableScriptAdapter} 实例
     */
    @Override
    public InvocableScriptAdapter prepareInvocableScript(ScriptModel scriptModel, ScriptBindingsConfigurer bindingsConfigurer) {
        final AbstractEvaluatableScriptAdapter evaluatable = prepareEvaluatableScript(scriptModel);
        return evaluatable.prepareInvokableScript(bindingsConfigurer);
    }

    /**
     * 将 {@link ScriptModel} 包装为可求值脚本适配器（优先编译）。
     * @param scriptModel must not be {@literal null}
     * @return {@link AbstractEvaluatableScriptAdapter} 实例
     */
    @Override
    public AbstractEvaluatableScriptAdapter prepareEvaluatableScript(ScriptModel scriptModel) {
        if (scriptModel == null) {
            throw new IllegalArgumentException("script must not be null");
        }

        if (scriptModel.getCode() == null || scriptModel.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("script must not be null or empty");
        }

        ScriptEngine engine = getPreparedScriptEngine(scriptModel);

        if (engine instanceof Compilable) {
            return new CompiledEvaluatableScriptAdapter(scriptModel, tryCompile(scriptModel, (Compilable) engine));
        }

        return new UncompiledEvaluatableScriptAdapter(scriptModel, engine);
    }

    /** 尝试编译脚本，失败时抛出 {@link ScriptCompilationException} @param scriptModel 脚本模型 @param engine 可编译引擎 */
    private CompiledScript tryCompile(ScriptModel scriptModel, Compilable engine) {
        try {
            return engine.compile(scriptModel.getCode());
        } catch (ScriptException e) {
            throw new ScriptCompilationException(scriptModel, e);
        }
    }

    /** 创建内存 {@link Script} 模型 @param realmId 领域 ID @param mimeType MIME 类型 @return 新 ScriptModel */
    @Override
    public ScriptModel createScript(String realmId, String mimeType, String scriptName, String scriptCode, String scriptDescription) {
        return new Script(null /* scriptId */, realmId, scriptName, mimeType, scriptCode, scriptDescription);
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {
        // 无操作
    }

    /** 为脚本查找或缓存 {@link ScriptEngine}（按 MIME 类型）。 */
    private ScriptEngine getPreparedScriptEngine(ScriptModel script) {
        // 优先从工厂缓存按 MIME 类型获取共享引擎
        if (factory.isEnableScriptEngineCache()) {
            ScriptEngine scriptEngine = factory.getScriptEngineCache().get(script.getMimeType());
            if (scriptEngine != null) return scriptEngine;
        }

        ScriptEngine scriptEngine = lookupScriptEngineFor(script);

        if (scriptEngine == null) {
            throw new IllegalStateException("Could not find ScriptEngine for script: " + script);
        }

        ServicesLogger.LOGGER.scriptEngineCreated(scriptEngine.getFactory().getEngineName(), scriptEngine.getFactory().getEngineVersion(), script.getMimeType());

        // Nashorn 引擎可跨线程共享并缓存
        if (factory.isEnableScriptEngineCache()) {
            factory.getScriptEngineCache().put(script.getMimeType(), scriptEngine);
        }

        return scriptEngine;
    }

    /** 根据脚本 MIME 类型通过 {@link ScriptEngineManager} 查找引擎 @param script 脚本模型 @return 脚本引擎或 null */
    private ScriptEngine lookupScriptEngineFor(ScriptModel script) {
        return new ScriptEngineManager().getEngineByMimeType(script.getMimeType());
    }
}
