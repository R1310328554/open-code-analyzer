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

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.keycloak.models.ScriptModel;

/**
 * 可调用脚本适配器：包装 {@link ScriptModel} 并实现 {@link Invocable} 接口。
 * <p>由 {@link ScriptingProvider#prepareInvocableScript} 创建，用于调用脚本中的函数与方法。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class InvocableScriptAdapter implements Invocable {

    /** 被包装的 {@link ScriptModel}。 */
    private final ScriptModel scriptModel;

    /** 已加载脚本代码的 {@link ScriptEngine} 实例。 */
    private final ScriptEngine scriptEngine;

    /**
     * 创建新的 {@link InvocableScriptAdapter} 实例。
     *
     * @param scriptModel  must not be {@literal null}
     * @param scriptEngine must not be {@literal null}
     */
    public InvocableScriptAdapter(ScriptModel scriptModel, ScriptEngine scriptEngine) {

        if (scriptModel == null) {
            throw new IllegalArgumentException("scriptModel must not be null");
        }

        if (scriptEngine == null) {
            throw new IllegalArgumentException("scriptEngine must not be null");
        }

        this.scriptModel = scriptModel;
        this.scriptEngine = scriptEngine;
    }

    /** 调用脚本对象上的方法，失败时包装为 {@link ScriptExecutionException}。 */
    @Override
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptExecutionException {

        try {
            return getInvocableEngine().invokeMethod(thiz, name, args);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new ScriptExecutionException(scriptModel, e);
        }
    }

    /** 调用脚本中的全局函数，失败时包装为 {@link ScriptExecutionException}。 */
    @Override
    public Object invokeFunction(String name, Object... args) throws ScriptExecutionException {
        try {
            return getInvocableEngine().invokeFunction(name, args);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new ScriptExecutionException(scriptModel, e);
        }
    }

    /** 获取脚本引擎实现的指定 Java 接口。 */
    @Override
    public <T> T getInterface(Class<T> clazz) {
        return getInvocableEngine().getInterface(clazz);
    }

    /** 获取脚本对象 thiz 实现的指定 Java 接口。 */
    @Override
    public <T> T getInterface(Object thiz, Class<T> clazz) {
        return getInvocableEngine().getInterface(thiz, clazz);
    }

    /**
     * 检查 {@link ScriptEngine} 上下文中是否已定义给定名称的绑定。
     *
     * @param name 绑定名称
     * @return 已定义返回 {@code true}
     */
    public boolean isDefined(String name) {

        Object candidate = scriptEngine.getContext().getAttribute(name);

        return candidate != null;
    }

    /** @return 将 {@link ScriptEngine} 转为 {@link Invocable} */
    private Invocable getInvocableEngine() {
        return (Invocable) scriptEngine;
    }
}
