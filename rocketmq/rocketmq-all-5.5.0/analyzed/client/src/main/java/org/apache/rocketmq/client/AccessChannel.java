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
package org.apache.rocketmq.client;

/**
 * 访问通道枚举：迁移至云服务时建议设为 CLOUD，自建 IDC 或启用消息轨迹时设为 LOCAL。
 */
public enum AccessChannel {
    /** 连接自建 IDC 集群。 */
    LOCAL,

    /** 连接 RocketMQ 云服务。 */
    CLOUD,
}
