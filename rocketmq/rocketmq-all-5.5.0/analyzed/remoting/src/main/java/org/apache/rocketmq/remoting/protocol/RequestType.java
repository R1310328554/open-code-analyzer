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

package org.apache.rocketmq.remoting.protocol;

/**
 * Remoting 请求子类型，目前仅定义流式请求。
 */
public enum RequestType {
    /** 流式 RPC 请求。 */
    STREAM((byte) 0);

    /** 协议单字节类型码。 */
    private final byte code;

    /** 绑定类型码。 */
    RequestType(byte code) {
        this.code = code;
    }

    /** 按字节码查找，未命中返回 null。 */
    public static RequestType valueOf(byte code) {
        for (RequestType requestType : RequestType.values()) {
            if (requestType.getCode() == code) {
                return requestType;
            }
        }
        return null;
    }

    /** 返回类型码。 */
    public byte getCode() {
        return code;
    }
}
