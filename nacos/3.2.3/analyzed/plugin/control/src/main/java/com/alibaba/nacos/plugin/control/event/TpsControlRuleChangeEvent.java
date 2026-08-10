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
 * TPS 管控规则变更事件，携带限流点名称与规则来源标志。
 *
 * <p>发布后经 {@link com.alibaba.nacos.plugin.control.rule.ControlRuleChangeActivator}
 * 解析并应用到对应 TPS 限流点。</p>
 *
 * @author liuzunfei
 * @version $Id: TpsControlPoint.java, v 0.1 2021年01月09日 12:38 PM liuzunfei Exp $
 */
public class TpsControlRuleChangeEvent extends Event {
    
    /** TPS 限流点名称（如接口或资源标识）。 */
    private String pointName;
    
    /** 是否从外部规则存储拉取最新内容。 */
    private boolean external;
    
    /**
     * 构造 TPS 规则变更事件。
     *
     * @param pointName TPS 限流点名称
     * @param external  是否使用外部存储作为规则来源
     */
    public TpsControlRuleChangeEvent(String pointName, boolean external) {
        this.pointName = pointName;
        this.external = external;
    }
    
    public String getPointName() {
        return pointName;
    }
    
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }
    
    public boolean isExternal() {
        return external;
    }
    
    public void setExternal(boolean external) {
        this.external = external;
    }
}
