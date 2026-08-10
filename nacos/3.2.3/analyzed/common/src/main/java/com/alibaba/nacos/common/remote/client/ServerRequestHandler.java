/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.remote.client;

import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.Response;

/**
 * 服务端推送请求处理器：当 Nacos Server 经双向流主动下发 {@link Request} 时，由注册的 Handler 生成 {@link Response} 回复。
 * ServerRequestHandler, to process the request from server side.
 *
 * @author liuzunfei
 * @version $Id: ServerRequestHandler.java, v 0.1 2020年07月14日 11:41 AM liuzunfei Exp $
 */
public interface ServerRequestHandler {
    
    /**
     * 处理服务端推送的请求并返回响应；{@code connection} 可用于读取对端能力表等上下文。
     * Handle request from server.
     *
     * @param request request
     * @param connection current connection, it can be used to know server ability
     * @return response.
     */
    Response requestReply(Request request, Connection connection);
    
}
