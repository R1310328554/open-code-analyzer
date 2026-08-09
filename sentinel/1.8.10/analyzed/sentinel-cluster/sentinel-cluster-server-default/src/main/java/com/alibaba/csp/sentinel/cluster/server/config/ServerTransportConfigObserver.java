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
package com.alibaba.csp.sentinel.cluster.server.config;

/**
 * 集群令牌服务端传输配置变更观察者。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public interface ServerTransportConfigObserver {

    /**
     * 服务端传输配置（如端口）变更时的回调。
     *
     * @param config 新的服务端传输配置
     */
    void onTransportConfigChange(ServerTransportConfig config);
}
