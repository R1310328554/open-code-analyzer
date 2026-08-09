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
package org.redisson.cluster;

import java.util.Objects;

/**
 * Redis 集群槽位闭区间 {@code [startSlot, endSlot]}。
 * <p>
 * 对应 {@code CLUSTER NODES} 输出中的单槽或区间表示（如 {@code 0-5460}），
 * 供 {@link ClusterNodeInfo} 与 {@link ClusterPartition} 维护槽位分配。
 *
 * @author Nikita Koksharov
 *
 */
public class ClusterSlotRange {

    /** 区间起始槽位（含）。 */
    private final int startSlot;
    /** 区间结束槽位（含）。 */
    private final int endSlot;

    /** @param startSlot 起始槽 @param endSlot 结束槽 */
    public ClusterSlotRange(int startSlot, int endSlot) {
        super();
        this.startSlot = startSlot;
        this.endSlot = endSlot;
    }

    public int getStartSlot() {
        return startSlot;
    }

    public int getEndSlot() {
        return endSlot;
    }

    /** 判断给定槽位是否落在此闭区间内。 */
    public boolean hasSlot(int slot) {
        return slot >= startSlot && slot <= endSlot;
    }

    /** 返回区间包含的槽位数量。 */
    public int size() {
        return endSlot - startSlot + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterSlotRange that = (ClusterSlotRange) o;
        return startSlot == that.startSlot && endSlot == that.endSlot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startSlot, endSlot);
    }

    @Override
    public String toString() {
        return "[" + startSlot + "-" + endSlot + "]";
    }



}
