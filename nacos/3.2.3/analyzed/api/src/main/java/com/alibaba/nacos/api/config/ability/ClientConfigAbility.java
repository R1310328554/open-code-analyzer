/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.ability;

import java.io.Serializable;

/**
 * Nacos 配置客户端能力描述。
 *
 * <p>用于客户端与服务端协商支持的扩展特性（如远程指标采集）。</p>
 *
 * @author liuzunfei
 * @version $Id: ClientConfigAbility.java, v 0.1 2021年01月24日 00:09 AM liuzunfei Exp $
 */
public class ClientConfigAbility implements Serializable {
    
    private static final long serialVersionUID = 2442741206510725737L;
    
    /** 是否支持远程获取指标数据。 */
    private boolean supportRemoteMetrics;
    
    /** 是否支持远程指标采集。 */
    public boolean isSupportRemoteMetrics() {
        return supportRemoteMetrics;
    }
    
    /** 设置是否支持远程指标采集。 */
    public void setSupportRemoteMetrics(boolean supportRemoteMetrics) {
        this.supportRemoteMetrics = supportRemoteMetrics;
    }
}
