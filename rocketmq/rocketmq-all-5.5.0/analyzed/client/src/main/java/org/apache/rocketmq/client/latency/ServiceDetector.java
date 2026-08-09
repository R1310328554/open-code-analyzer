/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.client.latency;

/**
 * 远程服务探测器：检测 Broker 等远端节点是否恢复正常。
 */
public interface ServiceDetector {

    /**
     * 探测远端服务是否可用。
     * @param endpoint 待检测的服务端点地址
     * @param timeoutMillis 超时毫秒数
     * @return true 表示服务已恢复
     */
    boolean detect(String endpoint, long timeoutMillis);
}
