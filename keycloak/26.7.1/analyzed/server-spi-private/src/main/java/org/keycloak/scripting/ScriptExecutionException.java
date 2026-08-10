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

import javax.script.ScriptException;

import org.keycloak.models.ScriptModel;

/**
 * 脚本执行异常：包装 {@link ScriptException} 并附加 {@link ScriptModel} 元数据。
 * <p>在脚本运行阶段由 {@link InvocableScriptAdapter} 等抛出。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class ScriptExecutionException extends RuntimeException {

    /**
     * @param script 执行失败的脚本模型
     * @param ex 底层 {@link ScriptException} 或其他异常
     */
    public ScriptExecutionException(ScriptModel script, Exception ex) {
        super("Could not execute script '" + script.getName() + "' problem was: " + ex.getMessage(), ex);
    }
}
