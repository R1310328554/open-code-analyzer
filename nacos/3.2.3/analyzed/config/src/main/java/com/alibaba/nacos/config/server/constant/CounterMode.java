/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.constant;

/**
 * 计数器操作模式枚举：增量 {@link #INCREMENT} 与减量 {@link #DECREMENT}，
 * 用于容量统计等场景的可逆切换。
 * counter mode.
 *
 * @author hexu.hxy
 * @date 2018/3/13
 */
public enum CounterMode {
    
    /**
     * 增量计数模式。
     * Increment.
     */
    INCREMENT,
    /**
     * 减量计数模式。
     * Decrement.
     */
    DECREMENT;
    
    /**
     * 反转当前模式：INCREMENT 与 DECREMENT 互换。
     * Reverse the two mode value.
     *
     * @return CounterMode
     */
    public CounterMode reverse() {
        if (INCREMENT == this) {
            return DECREMENT;
        }
        return INCREMENT;
    }
}
