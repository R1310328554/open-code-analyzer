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
 * 决策策略定义了与某策略关联的子策略如何求值以及如何得出最终决策。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public enum DecisionStrategy implements EnumWithStableIndex {

    /**
     * 至少一个子策略求值为正向决策时，整体决策才为正向。
     */
    AFFIRMATIVE(0),

    /**
     * 所有子策略均求值为正向决策时，整体决策才为正向。
     */
    UNANIMOUS(1),

    /**
     * 正向决策数量必须大于负向决策数量；若两者相等，则最终决策为负向。
     */
    CONSENSUS(2);

    /** 持久化用的稳定索引。 */
    private final int stableIndex;
    private static final Map<Integer, DecisionStrategy> BY_ID = EnumWithStableIndex.getReverseIndex(values());

    private DecisionStrategy(int stableIndex) {
        Objects.requireNonNull(stableIndex);
        this.stableIndex = stableIndex;
    }

    @Override
    public int getStableIndex() {
        return stableIndex;
    }

    /** 按稳定索引解析决策策略。 */
    public static DecisionStrategy valueOfInteger(Integer id) {
        return id == null ? null : BY_ID.get(id);
    }
}
