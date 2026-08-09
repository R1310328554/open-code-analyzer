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
package org.apache.rocketmq.proxy.common;

/**
 * Proxy 错误码枚举：映射 gRPC/Remoting 响应中的标准失败类型。
 */
public enum ProxyExceptionCode {
    /** Broker 名称无效或不存在。 */
    INVALID_BROKER_NAME,
    /** 事务消息关联数据未找到。 */
    TRANSACTION_DATA_NOT_FOUND,
    /** 鉴权失败或无权访问资源。 */
    FORBIDDEN,
    /** 消息属性与声明类型冲突。 */
    MESSAGE_PROPERTY_CONFLICT_WITH_TYPE,
    /** receipt handle 无效或已过期。 */
    INVALID_RECEIPT_HANDLE,
    /** Proxy 内部未预期错误。 */
    INTERNAL_SERVER_ERROR,
}
