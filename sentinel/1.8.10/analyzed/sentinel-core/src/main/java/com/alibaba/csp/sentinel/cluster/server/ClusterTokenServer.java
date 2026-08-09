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
package com.alibaba.csp.sentinel.cluster.server;

/**
 * 分布式流控的令牌服务端接口。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface ClusterTokenServer {

    /**
     * 启动 Sentinel 集群服务端。
     *
     * @throws Exception 发生任何错误时抛出
     */
    void start() throws Exception;

    /**
     * 停止 Sentinel 集群服务端。
     *
     * @throws Exception 发生任何错误时抛出
     */
    void stop() throws Exception;
}
