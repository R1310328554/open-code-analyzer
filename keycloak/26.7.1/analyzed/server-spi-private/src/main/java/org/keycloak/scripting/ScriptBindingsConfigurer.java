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

/**
 * 脚本绑定配置回调接口：为 {@link javax.script.ScriptEngine} 自定义 {@link Bindings}。
 * <p>由 {@link ScriptingProvider} 在准备脚本时调用。</p>
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
@FunctionalInterface
public interface ScriptBindingsConfigurer {

    /** 默认空配置器：不添加任何绑定。 */
    ScriptBindingsConfigurer EMPTY = new ScriptBindingsConfigurer() {

        @Override
        public void configureBindings(Bindings bindings) {
            // 无操作
        }
    };

    /** 向给定 {@link Bindings} 注入脚本运行时变量。 */
    void configureBindings(Bindings bindings);
}