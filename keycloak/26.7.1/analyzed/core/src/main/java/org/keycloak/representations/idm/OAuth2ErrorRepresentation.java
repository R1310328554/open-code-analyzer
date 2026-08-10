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

import org.keycloak.OAuth2Constants;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OAuth 2.0 错误响应的 JSON 表示，对应 {@code error} 与 {@code error_description} 字段。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuth2ErrorRepresentation {

    /** OAuth 2.0 错误码（如 {@code invalid_grant}）。 */
    private String error;
    /** 面向开发者的错误描述文本。 */
    private String errorDescription;

    /** 无参构造。 */
    public OAuth2ErrorRepresentation() {
    }

    /**
     * 构造带错误码与描述的 OAuth2 错误。
     *
     * @param error OAuth 2.0 错误码
     * @param errorDescription 错误描述
     */
    public OAuth2ErrorRepresentation(String error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
    }

    @JsonProperty(OAuth2Constants.ERROR)
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @JsonProperty(OAuth2Constants.ERROR_DESCRIPTION)
    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
