/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.tomcat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.ServletException;
import org.apache.catalina.Manager;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * Tomcat {@link org.apache.catalina.valves.ValveBase}：请求结束后将 Session 持久化到 Redis。
 * <p>在 {@link org.apache.catalina.connector.Request} 处理完成后调用
 * {@link RedissonSessionManager#store(jakarta.servlet.http.HttpSession)} 写回变更。
 * <p>配合 {@link UsageValve} 跟踪 Session 使用计数，避免并发更新冲突。
 *
 * @author Nikita Koksharov
 */
public class UpdateValve extends ValveBase {

    private static final String ALREADY_FILTERED_NOTE = UpdateValve.class.getName() + ".ALREADY_FILTERED_NOTE";

    private final AtomicInteger usage = new AtomicInteger(1);

    public UpdateValve() {
        super(true);
    }

    /** 递增 Valve 引用计数（{@link RedissonSessionManager} 生命周期管理）。 */
    public void incUsage() {
        usage.incrementAndGet();
    }

    /** 递减引用计数并返回当前值。 */
    public int decUsage() {
        return usage.decrementAndGet();
    }

    /** 委托后续 Valve；finally 块中将 Session 写回 Redis。 */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        if (getNext() == null) {
            return;
        }

        // 防止同一请求在 Valve 链中重复触发持久化
        if (request.getNote(ALREADY_FILTERED_NOTE) == null) {
            request.setNote(ALREADY_FILTERED_NOTE, Boolean.TRUE);
            try {
                getNext().invoke(request, response);
            } finally {
                request.removeNote(ALREADY_FILTERED_NOTE);
                if (request.getContext() == null) {
                    return;
                }

                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                try {
                    ClassLoader applicationClassLoader = request.getContext().getLoader().getClassLoader();
                    Thread.currentThread().setContextClassLoader(applicationClassLoader);
                    Manager manager = request.getContext().getManager();
                    ((RedissonSessionManager)manager).store(request.getSession(false));
                } finally {
                    Thread.currentThread().setContextClassLoader(classLoader);
                }
            }
        } else {
            getNext().invoke(request, response);
        }
    }

}
