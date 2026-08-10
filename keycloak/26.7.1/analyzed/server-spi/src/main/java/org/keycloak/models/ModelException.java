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

package org.keycloak.models;

import org.keycloak.common.util.Throwables;

/**
 * 模型层运行时异常基类，可携带国际化消息参数。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ModelException extends RuntimeException {

    private Object[] parameters;

    /** 默认构造。 */
    public ModelException() {
    }

    /** @param message 错误消息 */
    public ModelException(String message) {
        super(message);
    }

    /** @param message 消息模板
     * @param parameters 消息参数 */
    public ModelException(String message, Object ... parameters) {
        super(message);
        this.parameters = parameters;
    }

    /** @param message 错误消息
     * @param cause 原因 */
    public ModelException(String message, Throwable cause) {
        super(message, cause);
    }

    /** @return 消息格式化参数数组 */
    public Object[] getParameters() {
        return parameters;
    }

    /** @param parameters 消息格式化参数 */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    /** @param type 待检查的异常类型
     * @return 是否由给定类型引起 */
    @SafeVarargs
    public final boolean isCausedBy(Class<? extends Exception>... type) {
        return Throwables.isCausedBy(this, type);
    }
}
