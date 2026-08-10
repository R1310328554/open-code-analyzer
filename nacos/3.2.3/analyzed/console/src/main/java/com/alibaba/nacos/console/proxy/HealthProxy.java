/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.console.proxy;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.console.handler.HealthHandler;
import org.springframework.stereotype.Service;

/**
 * 健康检查代理：将控制台就绪探针请求委托给 {@link HealthHandler} 实现。
 * Proxy class for handling health check operations.
 *
 * @author zhangyukun
 */
@Service
public class HealthProxy {
    
    /** 健康检查 Handler 实现 */
    private final HealthHandler healthHandler;
    
    /** 注入健康检查 Handler。 */
    public HealthProxy(HealthHandler healthHandler) {
        this.healthHandler = healthHandler;
    }
    
    /**
     * 执行就绪探针，判断 Nacos 是否可接收请求。
     * Perform readiness check to determine if Nacos is ready to handle requests.
     *
     * @return 就绪检查结果
     */
    public Result<String> checkReadiness() throws NacosException {
        return healthHandler.checkReadiness();
    }
}
