/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.handler.impl.inner;

import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.console.handler.HealthHandler;
import com.alibaba.nacos.core.cluster.health.ModuleHealthCheckerHolder;
import com.alibaba.nacos.core.cluster.health.ReadinessResult;
import org.springframework.stereotype.Service;

/**
 * 健康检查 Inner Handler：委托 {@link ModuleHealthCheckerHolder} 执行就绪探针检测。
 * Implementation of HealthHandler that performs health check operations.
 *
 * @author zhangyukun
 */
@Service
@EnabledInnerHandler
public class HealthInnerHandler implements HealthHandler {
    
    /** 执行集群模块就绪检查，成功返回 ok，失败返回错误信息。 */
    @Override
    public Result<String> checkReadiness() {
        ReadinessResult result = ModuleHealthCheckerHolder.getInstance().checkReadiness();
        if (result.isSuccess()) {
            return Result.success("ok");
        }
        return Result.failure(result.getResultMessage());
    }
}
