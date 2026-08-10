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
import com.alibaba.nacos.console.handler.ServerStateHandler;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 服务器状态代理：委托 {@link ServerStateHandler} 查询运行状态、公告与控制台引导信息。
 * Proxy class for handling server state operations.
 *
 * @author zhangyukun
 */
@Service
public class ServerStateProxy {
    
    /** 服务器状态 Handler 实现 */
    private final ServerStateHandler serverStateHandler;
    
    /** 注入服务器状态 Handler。 */
    public ServerStateProxy(ServerStateHandler serverStateHandler) {
        this.serverStateHandler = serverStateHandler;
    }
    
    /**
     * 获取当前服务器运行状态键值对。
     * Get the current state of the server.
     *
     * @return 服务器状态 Map
     */
    public Map<String, String> getServerState() throws NacosException {
        return serverStateHandler.getServerState();
    }
    
    /**
     * 按语言获取控制台公告内容。
     * Get the announcement content based on the language.
     *
     * @param language 公告语言
     * @return 公告文本
     */
    public String getAnnouncement(String language) {
        return serverStateHandler.getAnnouncement(language);
    }
    
    /**
     * 获取控制台 UI 引导说明文案。
     * Get the console UI guide information.
     *
     * @return 引导信息文本
     */
    public String getConsoleUiGuide() {
        return serverStateHandler.getConsoleUiGuide();
    }
}
