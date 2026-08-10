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

package com.alibaba.nacos.naming.push.v2.task;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.remote.PushCallBack;

/**
 * 命名模块 RPC 推送回调接口。
 *
 * <p>扩展 {@link PushCallBack}，允许 {@link PushExecutor} 在 selector 过滤后回写实际推送的 {@link ServiceInfo}。</p>
 *
 * @author xiweng.yy
 */
public interface NamingPushCallback extends PushCallBack {
    
    /**
     * 设置经 selector 过滤后实际推送的 ServiceInfo（hosts 可能已变更）。
     * 详见 {@link com.alibaba.nacos.naming.push.v2.executor.PushExecutor} 实现。
     *
     * @param serviceInfo actual pushed service info
     */
    void setActualServiceInfo(ServiceInfo serviceInfo);
}
