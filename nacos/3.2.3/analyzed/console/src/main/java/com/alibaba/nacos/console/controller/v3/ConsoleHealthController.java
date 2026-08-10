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

package com.alibaba.nacos.console.controller.v3;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.console.paramcheck.ConsoleDefaultHttpParamExtractor;
import com.alibaba.nacos.console.proxy.HealthProxy;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制台 v3 健康检查 REST 控制器，暴露存活与就绪探针接口。
 * 映射路径 {@code /v3/console/health}，供 K8s 或负载均衡器探测 Nacos 运行状态。
 *
 * Controller class for handling health check operations.
 *
 * @author zhangyukun on:2024/8/27
 */
@NacosApi
@RestController()
@RequestMapping("/v3/console/health")
@ExtractorManager.Extractor(httpExtractor = ConsoleDefaultHttpParamExtractor.class)
public class ConsoleHealthController {
    
    /** 健康检查代理，封装就绪状态的后端查询逻辑。 */
    private final HealthProxy healthProxy;
    
    /**
     * 构造健康检查控制器。
     *
     * @param healthProxy 健康检查代理
     */
    public ConsoleHealthController(HealthProxy healthProxy) {
        this.healthProxy = healthProxy;
    }
    
    /**
     * 存活探针：判断 Nacos 是否处于不可自愈的故障状态。
     * Whether the Nacos is in broken states or not, and cannot recover except by being restarted.
     *
     * @return HTTP code equal to 200 indicates that Nacos is in right states. HTTP code equal to 500 indicates that
     * Nacos is in broken states.
     */
    @Since("3.0.0")
    @GetMapping("/liveness")
    public Result<String> liveness() {
        return Result.success("ok");
    }
    
    /**
     * 就绪探针：判断 Nacos 是否已准备好接收业务请求。
     * Ready to receive the request or not.
     *
     * @return HTTP code equal to 200 indicates that Nacos is ready. HTTP code equal to 500 indicates that Nacos is not
     * ready.
     */
    @Since("3.0.0")
    @GetMapping("/readiness")
    public ResponseEntity<Result<String>> readiness() throws NacosException {
        Result<String> ret = healthProxy.checkReadiness();
        if (ret.getCode() == 0) {
            return ResponseEntity.ok().body(ret);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ret);
        }
    }
    
}
