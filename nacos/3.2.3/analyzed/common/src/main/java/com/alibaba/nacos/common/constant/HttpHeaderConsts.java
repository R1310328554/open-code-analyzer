/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.constant;

/**
 * Nacos header constants.
 * <p>Nacos HTTP 请求/响应常用头字段名常量，供客户端与服务端统一引用。</p>
 *
 * @author ly
 */
public interface HttpHeaderConsts {
    
    /** 客户端版本号头 */
    String CLIENT_VERSION_HEADER = "Client-Version";
    /** User-Agent 头 */
    String USER_AGENT_HEADER = "User-Agent";
    /** 请求来源标识头 */
    String REQUEST_SOURCE_HEADER = "Request-Source";
    /** Content-Type 头 */
    String CONTENT_TYPE = "Content-Type";
    /** Content-Disposition 头 */
    String CONTENT_DISPOSITION = "Content-Disposition";
    /** Content-Length 头 */
    String CONTENT_LENGTH = "Content-Length";
    /** Accept-Charset 头 */
    String ACCEPT_CHARSET = "Accept-Charset";
    /** Accept-Encoding 头 */
    String ACCEPT_ENCODING = "Accept-Encoding";
    /** Content-Encoding 头 */
    String CONTENT_ENCODING = "Content-Encoding";
    /** 请求方标识（字段名 CONNECTION，值为 Requester） */
    String CONNECTION = "Requester";
    /** 请求唯一 ID 头 */
    String REQUEST_ID = "RequestId";
    /** 请求所属模块头 */
    String REQUEST_MODULE = "Request-Module";
    /** 应用名字段（app） */
    String APP_FILED = "app";
    /** 客户端 IP 字段 */
    String CLIENT_IP = "clientIp";
}
