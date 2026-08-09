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
package org.apache.rocketmq.remoting.netty;

/**
 * Netty 通道事件类型枚举。
 */
public enum NettyEventType {
    /** 物理连接建立。 */
    CONNECT,
    /** 连接关闭。 */
    CLOSE,
    /** 读写空闲超时。 */
    IDLE,
    /** 通道 I/O 异常。 */
    EXCEPTION,
    /** 通道激活可收发数据。 */
    ACTIVE
}
