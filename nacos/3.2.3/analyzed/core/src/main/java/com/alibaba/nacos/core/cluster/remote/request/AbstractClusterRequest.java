/*
 *
 *  * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.alibaba.nacos.core.cluster.remote.request;

import com.alibaba.nacos.api.remote.request.Request;

/**
 * 集群模块 RPC 请求基类：统一将 {@link #getModule()} 固定为 cluster。
 * Cluster request.
 *
 * @author xiweng.yy
 */
public abstract class AbstractClusterRequest extends Request {
    
    /** 集群模块标识常量。 */
    private static final String CLUSTER = "cluster";
    
    /** 返回集群模块名 cluster。 */
    @Override
    public String getModule() {
        return CLUSTER;
    }
}
