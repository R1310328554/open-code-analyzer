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

import javax.script.ScriptEngine;

import org.keycloak.models.ScriptModel;
import org.keycloak.provider.Provider;

/**
 * 脚本提供者接口：为 Keycloak 提供 JSR-223 脚本编译与执行能力。
 * <p>支持创建可调用与可求值脚本适配器，以及构建 {@link ScriptModel}。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public interface ScriptingProvider extends Provider {

    /**
     * 基于 {@link ScriptModel} 创建 {@link InvocableScriptAdapter}。
     * <p>使用 {@link ScriptBindingsConfigurer} 填充专用 {@link ScriptEngine} 的绑定。</p>
     *
     * @param scriptModel        the scriptModel to wrap
     * @param bindingsConfigurer populates the {@link javax.script.Bindings}
     * @return 可调用脚本适配器
     */
    InvocableScriptAdapter prepareInvocableScript(ScriptModel scriptModel, ScriptBindingsConfigurer bindingsConfigurer);

    /**
     * 基于 {@link ScriptModel} 创建 {@link EvaluatableScriptAdapter}。
     * <p>使用空绑定初始化专用 {@link ScriptEngine}。</p>
     *
     * @param scriptModel the scriptModel to wrap
     * @return 可求值脚本适配器
     */
    EvaluatableScriptAdapter prepareEvaluatableScript(ScriptModel scriptModel);

    /**
     * 创建新的 {@link ScriptModel} 实例。
     *
     * @param realmId 所属 realm ID
     * @param mimeType 脚本 MIME 类型
     * @param scriptName 脚本名称
     * @param scriptCode 脚本源代码
     * @param scriptDescription 脚本描述
     * @return 新脚本模型
     */
    ScriptModel createScript(String realmId, String mimeType, String scriptName, String scriptCode, String scriptDescription);
}
