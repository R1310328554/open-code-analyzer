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
package com.alibaba.csp.sentinel.command;

import java.util.*;

import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.command.handler.InterceptingCommandHandler;
import com.alibaba.csp.sentinel.spi.SpiLoader;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * 命令处理器提供者：通过 SPI 加载 {@link CommandHandler} 实现，
 * 解析 {@link CommandMapping} 注解获取命令名，并按需包装拦截器链。
 *
 * @author Eric Zhao
 */
public class CommandHandlerProvider implements Iterable<CommandHandler> {

    private final SpiLoader<CommandHandler> spiLoader = SpiLoader.of(CommandHandler.class);

    /**
     * 获取所有带 {@link CommandMapping} 注解的命令处理器，键为命令名。
     * 若存在 {@link CommandHandlerInterceptor}，则自动包装为 {@link InterceptingCommandHandler}。
     *
     * @return 命令名到处理器的映射
     */
    public Map<String, CommandHandler> namedHandlers() {
        Map<String, CommandHandler> map = new HashMap<String, CommandHandler>();
        List<CommandHandler> handlers = spiLoader.loadInstanceList();
        List<CommandHandlerInterceptor> commandHandlerInterceptors = SpiLoader.of(CommandHandlerInterceptor.class).loadInstanceListSorted();
        for (CommandHandler handler : handlers) {
            String name = parseCommandName(handler);
            if (StringUtil.isEmpty(name)) {
                continue;
            }
            // 收集对该命令生效的拦截器并包装为责任链
            if (!commandHandlerInterceptors.isEmpty()) {
                List<CommandHandlerInterceptor> interceptors = new ArrayList<>();
                for (CommandHandlerInterceptor commandHandlerInterceptor : commandHandlerInterceptors) {
                    if (commandHandlerInterceptor.shouldIntercept(name)) {
                        interceptors.add(commandHandlerInterceptor);
                    }
                }
                if (!interceptors.isEmpty()) {
                    handler = new InterceptingCommandHandler(handler, interceptors);
                }
            }
            map.put(name, handler);
        }
        return map;
    }

    /** 从处理器类上的 {@link CommandMapping} 注解解析命令名。 */
    private String parseCommandName(CommandHandler handler) {
        CommandMapping commandMapping = handler.getClass().getAnnotation(CommandMapping.class);
        if (commandMapping != null) {
            return commandMapping.name();
        } else {
            return null;
        }
    }

    @Override
    public Iterator<CommandHandler> iterator() {
        return spiLoader.loadInstanceList().iterator();
    }

    private static final CommandHandlerProvider INSTANCE = new CommandHandlerProvider();

    public static CommandHandlerProvider getInstance() {
        return INSTANCE;
    }
}
