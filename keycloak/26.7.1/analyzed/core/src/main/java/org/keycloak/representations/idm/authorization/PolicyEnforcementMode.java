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
package org.keycloak.representations.idm.authorization;

import java.util.Map;
import java.util.Objects;

import org.keycloak.util.EnumWithStableIndex;

/**
 * 策略执行模式，决定授权服务器如何处理未绑定策略的资源访问请求。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public enum PolicyEnforcementMode implements EnumWithStableIndex {

    /**
     * 强制模式：资源未关联策略时默认拒绝访问。
     */
    ENFORCING(0),

    /**
     * 宽松模式：资源未关联策略时默认允许访问。
     */
    PERMISSIVE(1),

    /**
     * 禁用模式：跳过策略评估，允许访问所有资源。
     */
    DISABLED(2);

    private final int stableIndex;
    private static final Map<Integer, PolicyEnforcementMode> BY_ID = EnumWithStableIndex.getReverseIndex(values());

    private PolicyEnforcementMode(int stableIndex) {
        Objects.requireNonNull(stableIndex);
        this.stableIndex = stableIndex;
    }

    /** @return 持久化用的稳定索引值 */
    @Override
    public int getStableIndex() {
        return stableIndex;
    }

    /** @param id 稳定索引值 */
    public static PolicyEnforcementMode valueOfInteger(Integer id) {
        return id == null ? null : BY_ID.get(id);
    }
}
