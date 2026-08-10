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

package com.alibaba.nacos.plugin.control.tps;

/**
 * TPS 监控模式枚举，决定超限时仅记录指标还是直接拒绝请求。
 *
 * @author liuzunfei
 * @version $Id: MonitorType.java, v 0.1 2021年01月12日 20:38 PM liuzunfei Exp $
 */
public enum MonitorType {
    
    /** 监控模式：超限仅记录，不拒绝请求。 */
    MONITOR("monitor", "only monitor ,not reject  request."),
    /** 拦截模式：超限直接拒绝请求。 */
    INTERCEPT("intercept", "reject  request if tps over limit");
    
    /** 模式类型标识。 */
    String type;
    
    /** 模式描述。 */
    String desc;
    
    MonitorType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
    
    /**
     * 获取模式类型标识字符串。
     *
     * @return 类型标识
     */
    public String getType() {
        return type;
    }
}
