/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.auth.impl.process;

import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import java.util.Properties;

/**
 * Nacos login processor.
 * <p>登录处理器 SPI：将客户端 {@link Properties} 转为对 Server 的登录请求，并返回可挂载到后续 RPC/HTTP 的 {@link LoginIdentityContext}。</p>
 *
 * @author Nacos
 */
public interface LoginProcessor {
    
    /**
     * send request to server and get result.
     * <p>向 Server 发送登录请求并解析响应；失败时返回 {@code null}。</p>
     *
     * @param properties request properties.
     * @return login identity context.
     */
    LoginIdentityContext getResponse(Properties properties);
    
}
