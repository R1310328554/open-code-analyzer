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

package com.alibaba.nacos.api.naming.ability;

import java.io.Serializable;

/**
 * Nacos 命名客户端能力描述。
 *
 * <p>在连接握手时上报客户端是否支持增量推送、远程指标等特性，供服务端协商协议行为。</p>
 *
 * @author liuzunfei
 * @version $Id: ClientNamingAbility.java, v 0.1 2021年01月24日 00:09 AM liuzunfei Exp $
 */
public class ClientNamingAbility implements Serializable {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 7643941846828882862L;
    
    /** 是否支持增量（Delta）实例推送。 */
    private boolean supportDeltaPush;
    
    /** 是否支持远程指标上报。 */
    private boolean supportRemoteMetric;
    
    /** 是否支持增量推送。 */
    public boolean isSupportDeltaPush() {
        return supportDeltaPush;
    }
    
    /** 设置是否支持增量推送。 */
    public void setSupportDeltaPush(boolean supportDeltaPush) {
        this.supportDeltaPush = supportDeltaPush;
    }
    
    /** 是否支持远程指标。 */
    public boolean isSupportRemoteMetric() {
        return supportRemoteMetric;
    }
    
    /** 设置是否支持远程指标。 */
    public void setSupportRemoteMetric(boolean supportRemoteMetric) {
        this.supportRemoteMetric = supportRemoteMetric;
    }
}
