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

package com.alibaba.nacos.client.naming.backups;

/**
 * 容灾开关值对象。
 *
 * <p>表示磁盘或外部数据源读取到的容灾模式是否启用。</p>
 *
 * @author zongkang.guo
 */
public class FailoverSwitch {
    
    /** 容灾模式是否启用。 */
    /** Failover switch enable. */
    /** 容灾开关是否开启。 */
    private final boolean enabled;
    
    /** 返回容灾开关状态。 */
    public boolean getEnabled() {
        return enabled;
    }
    
    /** 构造指定开关状态的容灾开关。 */
    public FailoverSwitch(boolean enabled) {
        this.enabled = enabled;
    }
}
