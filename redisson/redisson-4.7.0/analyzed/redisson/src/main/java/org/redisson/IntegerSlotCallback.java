/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson;

import java.util.Collection;
import java.util.List;

/**
 * Redis Cluster 多 slot 命令的 {@link Integer} 结果聚合回调。
 * <p>对各 slot 返回值求和；可选固定 {@link #createParams} 参数覆盖默认行为。
 *
 * @author Nikita Koksharov
 */
public class IntegerSlotCallback implements SlotCallback<Integer, Integer> {

    private final Object[] params;

    /** 使用默认参数策略构造。 */
    public IntegerSlotCallback() {
        this(null);
    }

    /** @param params 若非 null，{@link #createParams} 始终返回该固定参数数组 */
    public IntegerSlotCallback(Object[] params) {
        this.params = params;
    }

    /** 对各 slot 返回的整型值求和。 */
    @Override
    public Integer onResult(Collection<Integer> result) {
        return result.stream().mapToInt(r -> r).sum();
    }

    /** 若构造时指定了固定参数则直接返回，否则委托 {@link SlotCallback} 默认实现。 */
    @Override
    public Object[] createParams(List<Object> params) {
        if (this.params != null) {
            return this.params;
        }
        return SlotCallback.super.createParams(params);
    }

}
