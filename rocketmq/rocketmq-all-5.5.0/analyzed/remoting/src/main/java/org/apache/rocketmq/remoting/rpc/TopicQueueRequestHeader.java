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
package org.apache.rocketmq.remoting.rpc;

/**
 * 带 Topic 与队列 ID 的 RPC 请求头抽象基类。
 */
public abstract class TopicQueueRequestHeader extends TopicRequestHeader {

    /** 返回队列 ID。 */
    public abstract Integer getQueueId();
    /** 设置队列 ID。 */
    public abstract void setQueueId(Integer queueId);

}
