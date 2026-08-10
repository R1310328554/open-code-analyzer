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

package org.keycloak.forms.login.freemarker.model;

/**
 * OAuth 设备/授权码结果 Bean：向 {@code code.ftl} 模板提供授权码与错误信息。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class CodeBean {

    private final String code;
    private final String error;

    /** @param code 授权码 @param error 错误消息键或文本（成功时为 null） */
    public CodeBean(String code, String error) {
        this.code = code;
        this.error = error;
    }

    /** @return 存在 code 且无 error 时为 true */
    public boolean isSuccess() {
        return code != null && error == null;
    }

    /** @return 授权码 */
    public String getCode() {
        return code;
    }

    /** @return 错误信息 */
    public String getError() {
        return error;
    }
}
