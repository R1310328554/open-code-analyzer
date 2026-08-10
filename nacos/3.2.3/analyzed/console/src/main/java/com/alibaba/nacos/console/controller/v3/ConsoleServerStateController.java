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
import com.alibaba.nacos.api.model.v2.SupportedLanguage;
import com.alibaba.nacos.console.paramcheck.ConsoleDefaultHttpParamExtractor;
import com.alibaba.nacos.console.proxy.ServerStateProxy;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 控制台 v3 服务器状态 REST 控制器，提供运行态、公告与 UI 引导信息查询。
 * 映射路径 {@code /v3/console/server}。
 *
 * Controller for managing server state-related operations.
 *
 * @author zhangyukun on:2024/8/27
 */
@NacosApi
@RestController
@RequestMapping("/v3/console/server")
@ExtractorManager.Extractor(httpExtractor = ConsoleDefaultHttpParamExtractor.class)
public class ConsoleServerStateController {
    
    /** 服务器状态代理，封装状态、公告与引导信息的查询。 */
    private final ServerStateProxy serverStateProxy;
    
    /**
     * 构造服务器状态控制器。
     *
     * @param serverStateProxy 服务器状态代理
     */
    public ConsoleServerStateController(ServerStateProxy serverStateProxy) {
        this.serverStateProxy = serverStateProxy;
    }
    
    /**
     * 获取当前 Nacos 节点的运行状态 JSON。
     * Get server state of current server.
     *
     * @return state json.
     */
    @Since("3.0.0")
    @GetMapping("/state")
    public ResponseEntity<Map<String, String>> serverState() throws NacosException {
        Map<String, String> serverState = serverStateProxy.getServerState();
        return ResponseEntity.ok().body(serverState);
    }
    
    /**
     * 按指定语言获取控制台公告内容。
     * Get the announcement content based on the specified language.
     *
     * @param language Language for the announcement (default: "zh-CN")
     * @return Announcement content as a string wrapped in a Result object
     */
    @Since("3.0.0")
    @GetMapping("/announcement")
    public Result<String> getAnnouncement(
        @RequestParam(required = false, name = "language",
            defaultValue = "zh-CN") String language) {
        // 校验语言参数是否在支持列表内
        if (!SupportedLanguage.isSupported(language)) {
            return Result.failure("Unsupported language: " + language);
        }
        String announcement = serverStateProxy.getAnnouncement(language);
        return Result.success(announcement);
    }
    
    /**
     * 获取控制台 UI 引导说明文本。
     * Get the console UI guide information.
     *
     * @return Console UI guide information as a string wrapped in a Result object
     */
    @Since("3.0.0")
    @GetMapping("/guide")
    public Result<String> getConsoleUiGuide() {
        String guideInformation = serverStateProxy.getConsoleUiGuide();
        return Result.success(guideInformation);
    }
}
