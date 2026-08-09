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
package com.alibaba.csp.sentinel.transport.command;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HandlerAdapter} 适配器：识别 {@link SentinelApiHandler} 并委托其处理 Spring MVC 请求。
 * 默认 {@link Ordered#LOWEST_PRECEDENCE}，可通过 setOrder 调整。
 *
 * @author shenbaoyong
 */
public class SentinelApiHandlerAdapter implements HandlerAdapter, Ordered {

    /** Handler 排序，数值越小优先级越高。 */
    private int order = Ordered.LOWEST_PRECEDENCE;

    /** 设置适配器在 HandlerAdapter 链中的顺序。 */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    /** 仅支持 {@link SentinelApiHandler} 类型 handler。 */
    public boolean supports(Object handler) {
        return handler instanceof SentinelApiHandler;
    }

    @Override
    /** 委托 {@link SentinelApiHandler#handle} 处理请求，不返回视图。 */
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SentinelApiHandler sentinelApiHandler = (SentinelApiHandler) handler;
        sentinelApiHandler.handle(request, response);
        return null;
    }

    @Override
    /** 命令 API 不做缓存，固定返回 -1。 */
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }
}
