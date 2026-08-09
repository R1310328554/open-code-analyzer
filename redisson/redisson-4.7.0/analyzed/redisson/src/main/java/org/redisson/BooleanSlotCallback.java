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
 * 集群分槽批量命令的 {@link SlotCallback} 实现：合并各槽 {@code Boolean} 结果为逻辑或。
 * <p>用于 {@link org.redisson.command.CommandAsyncService} 的 read/writeBatched 等 API，
 * 任一分片返回 {@code true} 则整体结果为 {@code true}。
 *
 * @author Nikita Koksharov
 */
public class BooleanSlotCallback implements SlotCallback<Boolean, Boolean> {

    private final Object[] params;

    /** 使用默认 {@link SlotCallback#createParams} 构造命令参数。 */
    public BooleanSlotCallback() {
        this(null);
    }

    /** @param params 固定命令参数数组；非 null 时覆盖默认 createParams 行为 */
    public BooleanSlotCallback(Object[] params) {
        this.params = params;
    }

    /** 合并各槽结果：包含 {@code true} 则返回 {@code true}。 */
    @Override
    public Boolean onResult(Collection<Boolean> res) {
        return res.contains(true);
    }

    /** 若构造时指定了固定 params 则直接返回，否则委托接口默认实现。 */
    @Override
    public Object[] createParams(List<Object> params) {
        if (this.params != null) {
            return this.params;
        }
        return SlotCallback.super.createParams(params);
    }
}
