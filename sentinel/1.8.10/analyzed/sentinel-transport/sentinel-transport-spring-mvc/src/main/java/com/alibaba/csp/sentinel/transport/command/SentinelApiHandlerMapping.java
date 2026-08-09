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

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.log.CommandCenterLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring MVC 命令 API 路由映射：将请求 URI 映射为 {@link SentinelApiHandler}。
 * 监听 Spring Boot {@code WebServerInitializedEvent} 自动回填运行时端口。
 *
 * @author shenbaoyong
 */
public class SentinelApiHandlerMapping extends AbstractHandlerMapping implements ApplicationListener {

    /** Spring Boot Web 容器就绪事件类名，用于运行时探测。 */
    private static final String SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS = "org.springframework.boot.web.context.WebServerInitializedEvent";
    /** 探测到的 WebServerInitializedEvent 类，非 Spring Boot 环境为 null。 */
    private static Class webServerInitializedEventClass;

    static {
        try {
            webServerInitializedEventClass = ClassUtils.forName(SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS, null);
            RecordLog.info("[SentinelApiHandlerMapping] class {} is present, this is a spring-boot app, we can auto detect port", SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS);
        } catch (ClassNotFoundException e) {
            RecordLog.info("[SentinelApiHandlerMapping] class {} is not present, this is not a spring-boot app, we can not auto detect port", SPRING_BOOT_WEB_SERVER_INITIALIZED_EVENT_CLASS);
        }
    }

    /** 命令名到 {@link CommandHandler} 的全局注册表。 */
    final static Map<String, CommandHandler> handlerMap = new ConcurrentHashMap<>();

    /** 为 true 时不挂载拦截器，仅返回 handler 链。 */
    private boolean ignoreInterceptor = true;

    /** 设置较低优先级，避免覆盖业务 HandlerMapping。 */
    public SentinelApiHandlerMapping() {
        setOrder(Ordered.LOWEST_PRECEDENCE - 10);
    }

    @Override
    /** 按 URI（去掉前导 /）查找已注册命令，命中则返回 {@link SentinelApiHandler}。 */
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        String commandName = request.getRequestURI();
        if (commandName.startsWith("/")) {
            commandName = commandName.substring(1);
        }
        CommandHandler commandHandler = handlerMap.get(commandName);
        return commandHandler != null ? new SentinelApiHandler(commandHandler) : null;
    }

    @Override
    /** ignoreInterceptor 为 true 时跳过拦截器，否则走父类默认链。 */
    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        return ignoreInterceptor ? new HandlerExecutionChain(handler) : super.getHandlerExecutionChain(handler, request);
    }

    /** 设置是否在 Handler 链中忽略拦截器。 */
    public void setIgnoreInterceptor(boolean ignoreInterceptor) {
        this.ignoreInterceptor = ignoreInterceptor;
    }

    /** 注册单个命令处理器，重复命令名会被忽略并打 warn 日志。 */
    public static void registerCommand(String commandName, CommandHandler handler) {
        if (StringUtil.isEmpty(commandName) || handler == null) {
            return;
        }

        if (handlerMap.containsKey(commandName)) {
            CommandCenterLog.warn("[SentinelApiHandlerMapping] Register failed (duplicate command): " + commandName);
            return;
        }

        handlerMap.put(commandName, handler);
    }

    /** 批量注册命令处理器。 */
    public static void registerCommands(Map<String, CommandHandler> handlerMap) {
        if (handlerMap != null) {
            for (Map.Entry<String, CommandHandler> e : handlerMap.entrySet()) {
                registerCommand(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    /** Spring Boot 启动完成后从事件中解析 Web 端口并写入 {@link TransportConfig}。 */
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (webServerInitializedEventClass != null && webServerInitializedEventClass.isAssignableFrom(applicationEvent.getClass())) {
            Integer port = null;
            try {
                BeanWrapper beanWrapper = new BeanWrapperImpl(applicationEvent);
                port = (Integer) beanWrapper.getPropertyValue("webServer.port");
            } catch (Exception e) {
                RecordLog.warn("[SentinelApiHandlerMapping] resolve port from event " + applicationEvent + " fail", e);
            }
            if (port != null && TransportConfig.getPort() == null) {
                RecordLog.info("[SentinelApiHandlerMapping] resolve port {} from event {}", port, applicationEvent);
                TransportConfig.setRuntimePort(port);
            }
        }
    }
}
