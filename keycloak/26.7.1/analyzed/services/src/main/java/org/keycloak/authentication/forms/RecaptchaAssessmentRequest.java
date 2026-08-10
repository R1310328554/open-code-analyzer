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

package org.keycloak.authentication.forms;

import com.fasterxml.jackson.annotation.JsonProperty;

import static java.lang.String.format;

/**
 * reCAPTCHA Enterprise 评估请求体：封装 token、siteKey 与 expectedAction 事件数据。
 */
public class RecaptchaAssessmentRequest {
    /** 评估事件载荷。 */
    @JsonProperty("event")
    private Event event;

    /**
     * 构造 Enterprise 评估请求。
     * @param token 用户提交的 reCAPTCHA 响应令牌
     * @param siteKey 站点密钥
     * @param action 期望的动作名称
     */
    public RecaptchaAssessmentRequest(String token, String siteKey, String action) {
        this.event = new Event(token, siteKey, action);
    }

    public String toString() {
        return format("RecaptchaAssessmentRequest(event=%s)", this.getEvent());
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    /** reCAPTCHA Enterprise 评估事件字段。 */
    public static class Event {
        /** 用户响应令牌。 */
        @JsonProperty("token")
        private String token;

        /** 站点密钥。 */
        @JsonProperty("siteKey")
        private String siteKey;

        /** 期望的动作名称。 */
        @JsonProperty("expectedAction")
        private String action;

        public Event(String token, String siteKey, String action) {
            this.token = token;
            this.siteKey = siteKey;
            this.action = action;
        }

        public String toString() {
            return format("Event(token=%s, siteKey=%s, action=%s)",
                    this.getToken(), this.getSiteKey(), this.getAction());
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getSiteKey() {
            return siteKey;
        }

        public void setSiteKey(String siteKey) {
            this.siteKey = siteKey;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
