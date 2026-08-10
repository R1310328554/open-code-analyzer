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

package org.keycloak.representations.idm;

import java.util.List;

/**
 * Admin REST API 校验或业务错误的结构化表示，支持嵌套子错误列表。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ErrorRepresentation {
    /** 出错字段名（可为 null 表示全局错误）。 */
    private String field;
    /** 错误消息键或文本。 */
    private String errorMessage;
    /** 消息模板参数。 */
    private Object[] params;
    /** 嵌套子错误列表。 */
    private List<ErrorRepresentation> errors;

    /** 无参构造。 */
    public ErrorRepresentation() {
    }

    /**
     * 构造单条字段错误。
     *
     * @param field 出错字段名
     * @param errorMessage 错误消息
     * @param params 消息参数
     */
    public ErrorRepresentation(String field, String errorMessage, Object[] params) {
        super();
        this.field = field;
        this.errorMessage = errorMessage;
        this.params = params;
    }

    /** @return 出错字段名 */
    public String getField() {
        return field;
    }

    /** @param field 出错字段名 */
    public void setField(String field) {
        this.field = field;
    }

    /** @return 错误消息 */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** @param errorMessage 错误消息 */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /** @return 消息参数数组 */
    public Object[] getParams() {
        return this.params;
    }

    /** @param params 消息参数数组 */
    public void setParams(Object[] params) {
        this.params = params;
    }

    /** @param errors 嵌套子错误列表 */
    public void setErrors(List<ErrorRepresentation> errors) {
        this.errors = errors;
    }

    /** @return 嵌套子错误列表 */
    public List<ErrorRepresentation> getErrors() {
        return errors;
    }
}
