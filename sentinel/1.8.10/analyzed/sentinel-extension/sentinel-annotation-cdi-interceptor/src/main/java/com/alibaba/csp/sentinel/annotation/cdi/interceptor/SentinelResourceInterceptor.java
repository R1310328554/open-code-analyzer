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
package com.alibaba.csp.sentinel.annotation.cdi.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * CDI 拦截器：对 {@link SentinelResourceBinding} 标注的方法执行 Sentinel 入口/出口与降级逻辑。
 *
 * @author sea
 * @since 1.8.0
 */
@Interceptor
@SentinelResourceBinding
@Priority(0)
public class SentinelResourceInterceptor extends AbstractSentinelInterceptorSupport {

    @AroundInvoke
    Object aroundInvoke(InvocationContext ctx) throws Throwable {
        SentinelResourceBinding annotation = ctx.getMethod().getAnnotation(SentinelResourceBinding.class);
        if (annotation == null) {
            // 不应进入此分支
            throw new IllegalStateException("Wrong state for SentinelResource annotation");
        }

        String resourceName = getResourceName(annotation.value(), ctx.getMethod());
        EntryType entryType = annotation.entryType();
        int resourceType = annotation.resourceType();
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, resourceType, entryType, ctx.getParameters());
            Object result = ctx.proceed();
            return result;
        } catch (BlockException ex) {
            return handleBlockException(ctx, annotation, ex);
        } catch (Throwable ex) {
            Class<? extends Throwable>[] exceptionsToIgnore = annotation.exceptionsToIgnore();
            // 优先检查 exceptionsToIgnore
            if (exceptionsToIgnore.length > 0 && exceptionBelongsTo(ex, exceptionsToIgnore)) {
                throw ex;
            }
            if (exceptionBelongsTo(ex, annotation.exceptionsToTrace())) {
                traceException(ex);
                return handleFallback(ctx, annotation, ex);
            }

            // 无可用 fallback，原样抛出
            throw ex;
        } finally {
            if (entry != null) {
                entry.exit(1, ctx.getParameters());
            }
        }
    }
}
