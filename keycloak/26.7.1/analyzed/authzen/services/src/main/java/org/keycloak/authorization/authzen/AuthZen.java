/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.authzen;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * OpenID AuthZen 授权 API 1.0 的请求/响应数据模型与路径常量。
 * <p>
 * 本类为工具类，封装评估端点路径及 JSON 序列化 record 类型。
 */
public final class AuthZen {

    /** AuthZen 访问 API 根路径段。 */
    public static final String AUTHZEN_ACCESS_PATH = "access/v1";
    /** 单次访问评估端点路径。 */
    public static final String EVALUATION_PATH = AUTHZEN_ACCESS_PATH + "/evaluation";
    /** 批量访问评估端点路径。 */
    public static final String EVALUATIONS_PATH = AUTHZEN_ACCESS_PATH + "/evaluations";

    private AuthZen() {
    }

    /** 主体类型：客户端或用户。 */
    public enum SubjectType {
        CLIENT("client"),
        USER("user");

        private final String value;

        SubjectType(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static SubjectType fromValue(String value) {
            for (SubjectType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unsupported subject type: " + value);
        }
    }

    /** 访问评估请求中的主体（subject）。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Subject(
            @JsonProperty(required = true) SubjectType type,
            @JsonProperty(required = true) String id,
            Map<String, Object> properties) {}

    /** 访问评估请求中的资源（resource）。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Resource(
            @JsonProperty(required = true) String type,
            @JsonProperty(required = true) String id,
            Map<String, Object> properties) {}

    /** 访问评估请求中的动作（action）。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Action(String name) {}

    /** 单次访问评估请求体。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EvaluationRequest(
            @JsonProperty(required = true) Subject subject,
            @JsonProperty(required = true) Resource resource,
            @JsonProperty(required = true) Action action,
            Map<String, Object> context) {}

    /** 单次访问评估响应：决策布尔值及可选上下文。 */
    public record EvaluationResponse(boolean decision, Map<String, Object> context) {
        public EvaluationResponse(boolean decision) {
            this(decision, null);
        }
    }

    /** 批量评估中的单项（可覆盖顶层默认值）。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EvaluationItem(
            Subject subject,
            Resource resource,
            Action action,
            Map<String, Object> context) {}

    /** 批量评估语义：全部执行、首个拒绝即停、首个允许即停。 */
    public enum EvaluationsSemantic {
        @JsonProperty("execute_all")
        EXECUTE_ALL,

        @JsonProperty("deny_on_first_deny")
        DENY_ON_FIRST_DENY,

        @JsonProperty("permit_on_first_permit")
        PERMIT_ON_FIRST_PERMIT;
    }

    /** 批量评估选项（蛇形命名序列化）。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Options(EvaluationsSemantic evaluationsSemantic) {}

    /** 批量访问评估请求体。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EvaluationsRequest(
            Subject subject,
            Resource resource,
            Action action,
            Map<String, Object> context,
            Options options,
            List<EvaluationItem> evaluations) {}

    /** 批量访问评估响应体。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EvaluationsResponse(List<EvaluationResponse> evaluations) {}
}
