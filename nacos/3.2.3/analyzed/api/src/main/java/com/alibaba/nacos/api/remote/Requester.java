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

package com.alibaba.nacos.api.remote;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.Response;

/**
 * 远程连接请求发送接口，定义同步/异步/Future 三种调用方式。
 *
 * <p>gRPC 连接实现此接口，客户端通过它向 Nacos 服务端发送 {@link Request} 并接收 {@link Response}。</p>
 *
 * @author liuzunfei
 * @version $Id: Requester.java, v 0.1 2020年09月11日 4:05 PM liuzunfei Exp $
 */
public interface Requester {
    
    /**
     * 同步发送请求并阻塞等待响应。
     *
     * @param request      请求对象
     * @param timeoutMills 超时毫秒数
     * @return 服务端响应
     * @throws NacosException 发送或等待失败
     */
    Response request(Request request, long timeoutMills) throws NacosException;
    
    /**
     * 发送请求并返回 Future，由调用方自行阻塞等待。
     *
     * @param request 请求对象
     * @return 请求 Future
     * @throws NacosException 发送失败
     */
    RequestFuture requestFuture(Request request) throws NacosException;
    
    /**
     * 异步发送请求，结果通过回调通知。
     *
     * @param request         请求对象
     * @param requestCallBack 结果回调
     * @throws NacosException 发送失败
     */
    void asyncRequest(Request request, RequestCallBack requestCallBack) throws NacosException;
    
    /** 关闭底层连接并释放资源。 */
    void close();
    
}
