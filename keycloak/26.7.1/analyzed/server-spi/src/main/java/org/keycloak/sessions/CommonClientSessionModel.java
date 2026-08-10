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

package org.keycloak.sessions;

import java.util.Map;
import java.util.Objects;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.util.EnumWithStableIndex;

import org.infinispan.protostream.annotations.Proto;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 客户端会话公共模型：{@link AuthenticationSessionModel} 等的前辈抽象，含重定向 URI、动作与执行状态。
 *
 * Predecessor of AuthenticationSessionModel, ClientLoginSessionModel and ClientSessionModel (then action tickets). Maybe we will remove it later...
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface CommonClientSessionModel {

    /** @return 重定向 URI */
    String getRedirectUri();
    /** @param uri 重定向 URI */
    void setRedirectUri(String uri);

    /** @return 所属 Realm */
    RealmModel getRealm();
    /** @return 关联客户端 */
    ClientModel getClient();

    /** @return 当前动作标识 */
    String getAction();
    /** @param action 动作标识 */
    void setAction(String action);

    /** @return 协议名称 */
    String getProtocol();
    /** @param method 协议名称 */
    void setProtocol(String method);

    /** 客户端会话动作类型。 */
    enum Action {
        /** OAuth 授权同意。 */
        OAUTH_GRANT,
        /** 用户认证。 */
        AUTHENTICATE,
        /** 已登出。 */
        LOGGED_OUT,
        /** 登出进行中。 */
        LOGGING_OUT,
        /** 必需操作处理。 */
        REQUIRED_ACTIONS,
        /** 用户码验证（设备流等）。 */
        USER_CODE_VERIFICATION
    }

    @ProtoTypeId(65537) // see org.keycloak.Marshalling
    @Proto
    /** 认证器/必需操作执行状态（含稳定索引供序列化）。 */
    enum ExecutionStatus implements EnumWithStableIndex {
        /** 执行失败。 */
        FAILED(0),
        /** 执行成功。 */
        SUCCESS(1),
        /** 需要配置/setup。 */
        SETUP_REQUIRED(2),
        /** 已尝试。 */
        ATTEMPTED(3),
        /** 已跳过。 */
        SKIPPED(4),
        /** 已发起质询。 */
        CHALLENGED(5),
        /** 条件评估为真。 */
        EVALUATED_TRUE(6),
        /** 条件评估为假。 */
        EVALUATED_FALSE(7);

        private final int stableIndex;
        private static final Map<Integer, ExecutionStatus> BY_ID = EnumWithStableIndex.getReverseIndex(values());

        private ExecutionStatus(int stableIndex) {
            Objects.requireNonNull(stableIndex);
            this.stableIndex = stableIndex;
        }

        @Override
        /** @return 稳定序列化索引 */
        public int getStableIndex() {
            return stableIndex;
        }

        /** @param id 稳定索引
         * @return 对应枚举值或 null */
        public static ExecutionStatus valueOfInteger(Integer id) {
            return id == null ? null : BY_ID.get(id);
        }
    }
}
