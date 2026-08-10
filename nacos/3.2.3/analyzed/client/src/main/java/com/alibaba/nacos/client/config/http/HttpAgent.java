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

package com.alibaba.nacos.client.config.http;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.lifecycle.Closeable;

import java.util.Map;

/**
 * 配置模块 HTTP 通信代理接口。
 *
 * <p>封装与 Nacos 配置服务端的 GET/POST/DELETE 调用，由 {@link ServerHttpAgent} 实现。</p>
 *
 * @author Nacos
 */
public interface HttpAgent extends Closeable {
    
    /**
     * 启动代理，初始化 Nacos 服务端地址列表。
     *
     * @throws NacosException 获取地址列表失败时抛出
     */
    void start() throws NacosException;
    
    /**
     * 发起 HTTP GET 请求。
     *
     * @param path          请求路径
     * @param headers       请求头
     * @param paramValues   查询参数
     * @param encoding      字符编码
     * @param readTimeoutMs 读超时（毫秒）
     * @return HTTP 响应结果
     * @throws Exception 网络或 IO 异常
     */
    
    HttpRestResult<String> httpGet(String path, Map<String, String> headers,
        Map<String, String> paramValues,
        String encoding, long readTimeoutMs) throws Exception;
    
    /**
     * 发起 HTTP POST 请求（表单）。
     *
     * @param path          请求路径
     * @param headers       请求头
     * @param paramValues   表单参数
     * @param encoding      字符编码
     * @param readTimeoutMs 读超时（毫秒）
     * @return HTTP 响应结果
     * @throws Exception 网络或 IO 异常
     */
    HttpRestResult<String> httpPost(String path, Map<String, String> headers,
        Map<String, String> paramValues,
        String encoding, long readTimeoutMs) throws Exception;
    
    /**
     * 发起 HTTP DELETE 请求。
     *
     * @param path          请求路径
     * @param headers       请求头
     * @param paramValues   查询参数
     * @param encoding      字符编码
     * @param readTimeoutMs 读超时（毫秒）
     * @return HTTP 响应结果
     * @throws Exception 网络或 IO 异常
     */
    HttpRestResult<String> httpDelete(String path, Map<String, String> headers,
        Map<String, String> paramValues,
        String encoding, long readTimeoutMs) throws Exception;
    
    /**
     * 获取代理名称标识。
     *
     * @return 代理名称
     */
    String getName();
    
    /**
     * 获取命名空间。
     *
     * @return 命名空间字符串
     */
    String getNamespace();
    
    /**
     * 获取租户标识。
     *
     * @return 租户字符串
     */
    String getTenant();
    
    /**
     * 获取字符编码。
     *
     * @return 编码名称
     */
    String getEncode();
}
