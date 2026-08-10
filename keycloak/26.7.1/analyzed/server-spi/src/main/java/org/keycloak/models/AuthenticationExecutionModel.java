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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

import org.keycloak.util.EnumWithStableIndex;

/**
 * 认证流执行模型：描述认证流中的一个执行步骤（认证器或子流）。
 * <p>包含执行 ID、认证器 ID、需求级别、优先级及父子流关系。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AuthenticationExecutionModel implements Serializable {

    /** 按 {@link #priority} 升序比较执行步骤。 */
    public static class ExecutionComparator implements Comparator<AuthenticationExecutionModel> {
        /** 单例比较器实例。 */
        public static final ExecutionComparator SINGLETON = new ExecutionComparator();

        @Override
        public int compare(AuthenticationExecutionModel o1, AuthenticationExecutionModel o2) {
            return o1.priority - o2.priority;
        }
    }

    private String id;
    private String authenticatorConfig;
    private String authenticator;
    private String flowId;
    private boolean authenticatorFlow;
    private Requirement requirement;
    private int priority;
    private String parentFlow;

    /** @return 执行步骤唯一 ID */
    public String getId() {
        return id;
    }

    /** @param id 执行步骤 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 认证器配置组件 ID */
    public String getAuthenticatorConfig() {
        return authenticatorConfig;
    }

    /** @param authenticatorConfig 认证器配置 ID */
    public void setAuthenticatorConfig(String authenticatorConfig) {
        this.authenticatorConfig = authenticatorConfig;
    }

    /** @return 认证器提供者 ID */
    public String getAuthenticator() {
        return authenticator;
    }

    /** @param authenticator 认证器提供者 ID */
    public void setAuthenticator(String authenticator) {
        this.authenticator = authenticator;
    }

    /** @return 执行需求级别（必需/条件/备选/禁用） */
    public Requirement getRequirement() {
        return requirement;
    }

    /** @param requirement 需求级别 */
    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    /** @return 执行优先级（数值越小越先执行） */
    public int getPriority() {
        return priority;
    }

    /** @param priority 执行优先级 */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /** @return 父认证流 ID */
    public String getParentFlow() {
        return parentFlow;
    }

    /** @param parentFlow 父认证流 ID */
    public void setParentFlow(String parentFlow) {
        this.parentFlow = parentFlow;
    }

    /**
     * 若本执行为子流，返回指向 {@link AuthenticationFlowModel} 的 flowId。
     * If this execution is a flow, this is the flowId pointing to an AuthenticationFlowModel
     *
     * @return 子流 ID
     */
    public String getFlowId() {
        return flowId;
    }

    /** @param flowId 子认证流 ID */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /**
     * 引用的认证器是否为子流（而非单个认证器）。
     * Is the referenced authenticator a flow?
     *
     * @return {@code true} 表示子流
     */
    public boolean isAuthenticatorFlow() {
        return authenticatorFlow;
    }

    /** @param authenticatorFlow 是否为子流 */
    public void setAuthenticatorFlow(boolean authenticatorFlow) {
        this.authenticatorFlow = authenticatorFlow;
    }

    /** 认证执行需求级别，带稳定索引以支持序列化迁移。 */
    public enum Requirement implements EnumWithStableIndex {
        /** 必需：必须成功 */ REQUIRED(0),
        /** 条件：满足条件时执行 */ CONDITIONAL(1),
        /** 备选：同组中任一成功即可 */ ALTERNATIVE(2),
        /** 禁用：跳过 */ DISABLED(3);

        private final int stableIndex;
        private static final Map<Integer, Requirement> BY_ID = EnumWithStableIndex.getReverseIndex(values());

        private Requirement(int stableIndex) {
            Objects.requireNonNull(stableIndex);
            this.stableIndex = stableIndex;
        }

        /** @return 稳定索引值 */
        @Override
        public int getStableIndex() {
            return stableIndex;
        }

        /** @param id 稳定索引
         * @return 对应的需求级别，未知 ID 返回 {@code null} */
        public static Requirement valueOfInteger(Integer id) {
            return id == null ? null : BY_ID.get(id);
        }
    }

    /** @return 需求级别是否为 REQUIRED */
    public boolean isRequired() {
        return requirement == Requirement.REQUIRED;
    }
    /** @return 需求级别是否为 CONDITIONAL */
    public boolean isConditional() {
        return requirement == Requirement.CONDITIONAL;
    }
    /** @return 需求级别是否为 ALTERNATIVE */
    public boolean isAlternative() {
        return requirement == Requirement.ALTERNATIVE;
    }
    /** @return 需求级别是否为 DISABLED */
    public boolean isDisabled() {
        return requirement == Requirement.DISABLED;
    }
    /** @return 是否已启用（非 DISABLED） */
    public boolean isEnabled() {
        return requirement != Requirement.DISABLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthenticationExecutionModel that = (AuthenticationExecutionModel) o;

        if (id == null || that.id == null) return false;
        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
