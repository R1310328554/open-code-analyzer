/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.operator.crds.v2beta1;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Kubernetes 风格的状态条件基类，供各 CR status 复用。
 *
 * <p>序列化时使用字符串形式的 {@code status}（True/False/Unknown），
 * Java 侧通过 {@link Boolean} 便捷访问。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusCondition {
    /** 条件布尔值的 Kubernetes 枚举表示。 */
    public enum Status {
        True,
        False,
        Unknown
    }

    /** 条件类型标识，如 Ready、HasErrors。 */
    private String type;
    /** 条件状态字符串，默认为 Unknown。 */
    private String status = Status.Unknown.name();
    /** 人类可读的状态说明。 */
    private String message;
    /** 上次状态发生变更的 ISO8601 时间戳。 */
    private String lastTransitionTime;
    /** 观察到该条件时 CR 的 generation。 */
    private Long observedGeneration;

    /** 无参构造，供 Jackson/Builder 使用。 */
    public StatusCondition() {
    }

    /**
     * 全字段构造。
     *
     * @param type 条件类型
     * @param status 条件是否为 True；null 表示 Unknown
     * @param message 说明信息
     * @param lastTransitionTime 上次变更时间
     * @param observedGeneration 观察到的 generation
     */
    public StatusCondition(String type, Boolean status, String message, String lastTransitionTime,
            Long observedGeneration) {
        this.type = type;
        this.message = message;
        this.lastTransitionTime = lastTransitionTime;
        this.observedGeneration = observedGeneration;
        this.setStatus(status);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** 以 {@link Boolean} 形式读取条件状态；Unknown 时返回 null。 */
    @JsonIgnore
    public Boolean getStatus() {
        if (status == null || Status.Unknown.name().equals(status)) {
            return null;
        }
        return Status.True.name().equals(status);
    }

    /** 序列化/反序列化用的 status 字符串字段。 */
    @JsonProperty("status")
    public String getStatusString() {
        return status;
    }

    @JsonProperty("status")
    public void setStatusString(String status) {
        this.status = status;
    }

    /** 以 {@link Boolean} 设置条件状态，内部转换为 True/False/Unknown 字符串。 */
    @JsonIgnore
    public void setStatus(Boolean status) {
        if (status == null) {
            this.status = Status.Unknown.name();
        } else if (status) {
            this.status = Status.True.name();
        } else {
            this.status = Status.False.name();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLastTransitionTime() {
        return lastTransitionTime;
    }

    public void setLastTransitionTime(String lastTransitionTime) {
        this.lastTransitionTime = lastTransitionTime;
    }

    public Long getObservedGeneration() {
        return observedGeneration;
    }

    public void setObservedGeneration(Long observedGeneration) {
        this.observedGeneration = observedGeneration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatusCondition that = (StatusCondition) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getStatus(), that.getStatus()) && Objects.equals(getMessage(), that.getMessage())
                && Objects.equals(getLastTransitionTime(), that.getLastTransitionTime())
                && Objects.equals(getObservedGeneration(), that.getObservedGeneration());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getStatus(), getMessage(), getObservedGeneration(), getLastTransitionTime());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "type='" + type + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }

}
