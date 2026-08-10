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

package com.alibaba.nacos.lock.aspect;

import com.alibaba.nacos.api.lock.remote.request.LockOperationRequest;
import com.alibaba.nacos.api.lock.remote.response.LockOperationResponse;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.lock.monitor.LockMetricsMonitor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁远程请求切面：拦截 {@link com.alibaba.nacos.lock.remote.rpc.handler.LockRequestHandler} 统计 QPS 与耗时。
 *
 * <p>通过 {@link LockMetricsMonitor} 记录总量、成功数及处理时延。</p>
 *
 * @author goumang.zh@alibaba-inc.com
 */
@Aspect
@Component
public class RequestLockAspect {
    
    /**
     * 环绕锁 RPC 处理：计数并记录 handler 耗时。
     */
    @SuppressWarnings("checkstyle:linelength")
    @Around(
        value = "execution(* com.alibaba.nacos.core.remote.RequestHandler.handleRequest(..)) && target(com.alibaba.nacos.lock.remote.rpc.handler.LockRequestHandler) && args(request, meta)",
        argNames = "pjp,request,meta")
    /**
     * 锁操作指标采集切点。
     *
     * @param pjp 连接点
     * @param request 锁操作请求
     * @param meta 请求元数据
     * @return 原 handler 返回值
     */
    public Object lockMeterPoint(ProceedingJoinPoint pjp, LockOperationRequest request,
        RequestMeta meta)
        throws Throwable {
        long st = System.currentTimeMillis();
        try {
            LockMetricsMonitor.getTotalMeter(request.getLockOperationEnum()).incrementAndGet();
            LockOperationResponse result = (LockOperationResponse) pjp.proceed();
            if (result.isSuccess()) {
                LockMetricsMonitor.getSuccessMeter(request.getLockOperationEnum())
                    .incrementAndGet();
            }
            return result;
        } finally {
            long rt = System.currentTimeMillis() - st;
            LockMetricsMonitor.getLockHandlerTimer().record(rt, TimeUnit.MILLISECONDS);
        }
    }
}
