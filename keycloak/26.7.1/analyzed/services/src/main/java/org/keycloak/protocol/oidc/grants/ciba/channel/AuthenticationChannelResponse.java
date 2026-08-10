/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.protocol.oidc.grants.ciba.channel;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * CIBA 认证通道 HTTP 响应体：认证设备（AD）对后台认证请求的异步处理结果。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AuthenticationChannelResponse {

    /** 认证通道处理状态 */
    public enum Status {
        /** 认证成功 */
        SUCCEED,
        /** 用户拒绝或未授权 */
        UNAUTHORIZED,
        /** 用户取消认证 */
        CANCELLED;
    }

    /** 当前响应状态 */
    private Status status;

    /** 附加响应参数 */
    private Map<String, String> additionalParams = new HashMap<>();

    /** 无参构造，供 Jackson 反射反序列化 */
    public AuthenticationChannelResponse() {
        // for reflection
    }

    /**
     * 指定状态构造响应。
     * @param status 认证结果状态
     */
    public AuthenticationChannelResponse(Status status) {
        this.status = status;
    }

    /** @return 认证状态 */
    public Status getStatus() {
        return status;
    }

    /** 设置认证状态 */
    public void setStatus(Status status) {
        this.status = status;
    }

    /** @return 附加参数 */
    @JsonAnyGetter
    public Map<String, String> getAdditionalParams() {
        return additionalParams;
    }

    /** 设置单个附加响应参数 */
    @JsonAnySetter
    public void setAdditionalParams(String name, String value) {
        this.additionalParams.put(name, value);
    }
}
