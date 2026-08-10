/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.OAuthErrorException;

/**
 * 客户端策略执行异常：携带 OAuth 错误码、详情与 HTTP 状态；通知型用法不填充堆栈。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientPolicyException extends Exception {

    /** OAuth 错误码，默认 invalid_request。 */
    private String error = OAuthErrorException.INVALID_REQUEST;
    /** 错误详情描述。 */
    private String errorDetail ="NA";
    /** HTTP 响应状态，默认 400。 */
    private Status errorStatus = Response.Status.BAD_REQUEST;

    /** @param error OAuth 错误码 */
    public ClientPolicyException(String error) {
        super(error);
        setError(error);
    }

    /** @param error OAuth 错误码
     * @param errorDetail 错误详情 */
    public ClientPolicyException(String error, String errorDetail) {
        super(error);
        setError(error);
        setErrorDetail(errorDetail);
    }

    /** @param error OAuth 错误码
     * @param errorDetail 错误详情
     * @param errorStatus HTTP 状态 */
    public ClientPolicyException(String error, String errorDetail, Status errorStatus) {
        super(error);
        setError(error);
        setErrorDetail(errorDetail);
        setErrorStatus(errorStatus);
    }

    /** @param error OAuth 错误码
     * @param errorDetail 错误详情
     * @param throwable 根因 */
    public ClientPolicyException(String error, String errorDetail, Throwable throwable) {
        super(throwable);
        setError(error);
        setErrorDetail(errorDetail);
    }

    /** @param error OAuth 错误码
     * @param errorDetail 错误详情
     * @param errorStatus HTTP 状态
     * @param throwable 根因 */
    public ClientPolicyException(String error, String errorDetail, Status errorStatus, Throwable throwable) {
        super(throwable);
        setError(error);
        setErrorDetail(errorDetail);
        setErrorStatus(errorStatus);
    }

    /** @return OAuth 错误码 */
    public String getError() {
        return error;
    }

    /** @param error OAuth 错误码 */
    public void setError(String error) {
        this.error = error;
    }

    /** @return 错误详情 */
    public String getErrorDetail() {
        return errorDetail;
    }

    /** @param errorDetail 错误详情 */
    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    /** @return HTTP 响应状态 */
    public Status getErrorStatus() {
        return errorStatus;
    }

    /** @param errorStatus HTTP 响应状态 */
    public void setErrorStatus(Status errorStatus) {
        this.errorStatus = errorStatus;
    }

    /**
     * 通知型异常不填充堆栈跟踪。
     * If {@link ClientPolicyException} is used to notify the event so that it needs not to have stack trace.
     * @return always null
     */
    @Override
    public Throwable fillInStackTrace() {
        return null;
    }

}
