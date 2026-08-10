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

package com.alibaba.nacos.console.handler;

import com.alibaba.nacos.api.exception.NacosException;

import java.util.Map;

/**
 * 控制台服务器状态处理器接口：暴露运行状态、公告与 UI 引导信息。
 * Interface for handling server state operations.
 *
 * @author zhangyukun
 */
public interface ServerStateHandler {
    
    /**
     * 获取当前服务器运行状态键值对（版本、模式、功能开关等）。
     * Get the current state of the server.
     *
     * @return 服务器状态映射
     * @throws NacosException 读取状态时发生错误
     */
    Map<String, String> getServerState() throws NacosException;
    
    /**
     * 按语言获取控制台公告内容。
     * Get the announcement content based on the language.
     *
     * @param language 公告语言标识
     * @return 公告正文
     */
    String getAnnouncement(String language);
    
    /**
     * 获取控制台 UI 使用引导信息。
     * Get the console UI guide information.
     *
     * @return UI 引导文案或配置
     */
    String getConsoleUiGuide();
}
