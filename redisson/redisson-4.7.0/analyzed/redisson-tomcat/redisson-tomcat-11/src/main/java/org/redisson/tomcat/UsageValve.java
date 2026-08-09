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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tomcat Valve：在请求处理期间标记 {@link RedissonSession} 为“使用中”。
 * <p>通过 {@link RedissonSession#startUsage()} / {@link RedissonSession#endUsage()}
 * 防止后台线程在请求未完成时覆盖 Session 状态。
 *
 * @author Nikita Koksharov
 */
public class UsageValve extends ValveBase {

    private static final String ALREADY_FILTERED_NOTE = UsageValve.class.getName() + ".ALREADY_FILTERED_NOTE";

    private final AtomicInteger usage = new AtomicInteger(1);

    public UsageValve() {
        super(true);
    }

    /** 递增 Valve 引用计数。 */
    public void incUsage() {
        usage.incrementAndGet();
    }

    /** 递减引用计数并返回当前值。 */
    public int decUsage() {
        return usage.decrementAndGet();
    }

    /** 请求前 {@code startUsage}，完成后 {@code endUsage}，再委托 Valve 链。 */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        if (getNext() == null) {
            return;
        }

        // 防止同一请求重复进入使用计数逻辑
        if (request.getNote(ALREADY_FILTERED_NOTE) == null) {
            request.setNote(ALREADY_FILTERED_NOTE, Boolean.TRUE);
            try {
                if (request.getContext() != null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        RedissonSession s = (RedissonSession) request.getContext().getManager().findSession(session.getId());
                        if (s != null) {
                            s.startUsage();
                        }
                    }
                }

                getNext().invoke(request, response);
            } finally {
                request.removeNote(ALREADY_FILTERED_NOTE);
                if (request.getContext() != null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        RedissonSession s = (RedissonSession) request.getContext().getManager().findSession(session.getId());
                        if (s != null) {
                            s.endUsage();
                        }
                    }
                }
            }
        } else {
            getNext().invoke(request, response);
        }
    }

}
