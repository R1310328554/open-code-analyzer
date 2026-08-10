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

package org.keycloak.events.admin;

import java.util.Map;
import java.util.Objects;

import org.keycloak.util.EnumWithStableIndex;

/**
 * 管理 REST 操作类型，含稳定整数索引供事件存储序列化。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public enum OperationType implements EnumWithStableIndex {

    /** 创建资源。 */
    CREATE(0),
    /** 更新资源。 */
    UPDATE(1),
    /** 删除资源。 */
    DELETE(2),
    /** 非 CRUD 的管理动作（如触发 required action）。 */
    ACTION(3);

    private final int stableIndex;
    private static final Map<Integer, OperationType> BY_ID = EnumWithStableIndex.getReverseIndex(values());

    private OperationType(int stableIndex) {
        Objects.requireNonNull(stableIndex);
        this.stableIndex = stableIndex;
    }

    @Override
    public int getStableIndex() {
        return stableIndex;
    }

    /** 按稳定整数索引解析操作类型；{@code id} 为 {@code null} 时返回 {@code null}。 */
    public static OperationType valueOfInteger(Integer id) {
        return id == null ? null : BY_ID.get(id);
    }
}
