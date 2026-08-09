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
package org.apache.rocketmq.remoting.common;

/**
 * V2 心跳响应结果：协议版本、订阅变更与支持 V2 标志。
 */
public class HeartbeatV2Result {
    /** 心跳协议版本号。 */
    private int version = 0;
    /** 客户端订阅是否发生变更。 */
    private boolean isSubChange = false;
    /** 对端是否支持 V2 心跳协议。 */
    private boolean isSupportV2 = false;

    /** 构造心跳 V2 结果对象。 */
    public HeartbeatV2Result(int version, boolean isSubChange, boolean isSupportV2) {
        this.version = version;
        this.isSubChange = isSubChange;
        this.isSupportV2 = isSupportV2;
    }

    /** 返回协议版本。 */
    public int getVersion() {
        return version;
    }

    /** 设置协议版本。 */
    public void setVersion(int version) {
        this.version = version;
    }

    /** 订阅是否变更。 */
    public boolean isSubChange() {
        return isSubChange;
    }

    /** 设置订阅变更标志。 */
    public void setSubChange(boolean subChange) {
        isSubChange = subChange;
    }

    /** 是否支持 V2 心跳。 */
    public boolean isSupportV2() {
        return isSupportV2;
    }

    /** 设置 V2 支持标志。 */
    public void setSupportV2(boolean supportV2) {
        isSupportV2 = supportV2;
    }
}
