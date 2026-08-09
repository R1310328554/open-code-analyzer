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
package org.apache.rocketmq.broker.client;

/**
 * 生产者变更监听器：{@code ProducerManager} 在组或客户端状态变化时回调。
 * <p>
 * 事件类型见 {@link ProducerGroupEvent}。
 */
public interface ProducerChangeListener {

    /** 处理指定生产者组事件及关联客户端通道信息。 */
    void handle(ProducerGroupEvent event, String group, ClientChannelInfo clientChannelInfo);
}
