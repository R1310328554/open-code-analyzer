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

package org.apache.rocketmq.broker.lite;

/**
 * Lite 控制面事件监听器：订阅/退订 LMQ 时回调。
 */
public interface LiteCtlListener {

    /** 客户端注册对指定 LMQ 的 lite 订阅。 */
    void onRegister(String clientId, String group, String lmqName);

    /** 客户端取消对指定 LMQ 的 lite 订阅。 */
    void onUnregister(String clientId, String group, String lmqName);

    /** 客户端断开或 group 下全部 lite 订阅被移除。 */
    void onRemoveAll(String clientId, String group);

}
