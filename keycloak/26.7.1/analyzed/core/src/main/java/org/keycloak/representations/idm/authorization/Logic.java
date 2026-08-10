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
 * 策略逻辑模式，决定如何将单条策略的评估结果转换为最终决策。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public enum Logic implements EnumWithStableIndex {

    /**
     * 正向逻辑：最终决策与策略评估结果一致。
     */
    POSITIVE(0),

    /**
     * 反向逻辑：最终决策为策略评估结果的逻辑取反。
     */
    NEGATIVE(1);

    private final int stableIndex;
    private static final Map<Integer, Logic> BY_ID = EnumWithStableIndex.getReverseIndex(values());

    private Logic(int stableIndex) {
        Objects.requireNonNull(stableIndex);
        this.stableIndex = stableIndex;
    }

    /** @return 持久化用的稳定索引值 */
    @Override
    public int getStableIndex() {
        return stableIndex;
    }

    /** @param id 稳定索引值 */
    public static Logic valueOfInteger(Integer id) {
        return id == null ? null : BY_ID.get(id);
    }
}
