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
 * 带 Topic 字段的 RPC 请求头抽象基类，支持逻辑标识 lo。
 */
public abstract class TopicRequestHeader extends RpcRequestHeader {
    /** 逻辑标识（logical），用于区分同名 Topic 的不同逻辑视图。 */
    protected Boolean lo;

    /** 返回 Topic 名称。 */
    public abstract String getTopic();
    /** 设置 Topic 名称。 */
    public abstract void setTopic(String topic);

    /** 返回逻辑标识 lo。 */
    public Boolean getLo() {
        return lo;
    }
    /** 设置逻辑标识 lo。 */
    public void setLo(Boolean lo) {
        this.lo = lo;
    }
}
