/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * 连接数限流规则变更事件，通知管控插件重新加载并应用连接规则。
 *
 * <p>{@link com.alibaba.nacos.plugin.control.rule.ControlRuleChangeActivator}
 * 订阅此事件，按 {@code external} 标志决定从外部存储或本地磁盘读取规则。</p>
 *
 * @author zunfei.lzf
 */
public class ConnectionLimitRuleChangeEvent extends Event {
    
    /** 是否从外部规则存储拉取最新内容。 */
    private boolean external;
    
    /**
     * 构造连接限流规则变更事件。
     *
     * @param external 是否使用外部存储作为规则来源
     */
    public ConnectionLimitRuleChangeEvent(boolean external) {
        this.external = external;
    }
    
    public boolean isExternal() {
        return external;
    }
    
    public void setExternal(boolean external) {
        this.external = external;
    }
}
