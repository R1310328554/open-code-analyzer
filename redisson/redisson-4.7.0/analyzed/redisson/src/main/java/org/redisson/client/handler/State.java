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
package org.redisson.client.handler;

/**
 * RESP 协议解码过程中的可变状态容器。
 * <p>
 * 跟踪嵌套层级、批量命令索引及临时解码值。
 *
 * @author Nikita Koksharov
 *
 */
public class State {

    /** 当前批量命令在批次中的索引。 */
    private int batchIndex;

    /** 嵌套数组/多段回复的解码层级，初始为 -1。 */
    private int level = -1;

    /** 解码器暂存的中间结果。 */
    private Object value;

    /** 创建默认解码状态。 */
    public State() {
    }

    /** 返回暂存的解码值。 */
    public <T> T getValue() {
        return (T) value;
    }

    /** 设置暂存的解码值。 */
    public void setValue(Object value) {
        this.value = value;
    }

    /** 返回当前嵌套解码层级。 */
    public int getLevel() {
        return level;
    }

    /** 进入更深层嵌套时递增层级。 */
    public void incLevel() {
        level++;
    }
    
    /** 退出嵌套时递减层级。 */
    public void decLevel() {
        level--;
    }
    
    /** 设置批量命令索引。 */
    public void setBatchIndex(int index) {
        this.batchIndex = index;
    }
    /** 返回批量命令索引。 */
    public int getBatchIndex() {
        return batchIndex;
    }

    @Override
    public String toString() {
        return "State [batchIndex=" + batchIndex + ", level=" + level + "]";
    }

}
