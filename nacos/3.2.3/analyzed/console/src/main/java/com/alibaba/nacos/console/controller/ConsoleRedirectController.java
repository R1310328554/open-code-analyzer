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
 */

package com.alibaba.nacos.console.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 控制台根路径重定向：根据 {@code nacos.console.ui.default} 将 {@code /} 转发到新/旧 UI。
 * Controller to redirect root path to the default console UI.
 *
 * <p>The default UI version is controlled by the property {@code nacos.console.ui.default}.
 * Supported values: "next" (new UI) and "legacy" (old UI). Defaults to "next".
 *
 * @author zhuoguang
 */
@Controller
public class ConsoleRedirectController {
    
    /** 默认 UI 版本配置项键名 */
    private static final String PROPERTY_DEFAULT_UI = "nacos.console.ui.default";
    
    /** 旧版控制台 UI 标识 */
    private static final String UI_LEGACY = "legacy";
    
    /** 新版控制台 UI 标识 */
    private static final String UI_NEXT = "next";
    
    /**
     * 根路径入口：按配置重定向到 {@code /next/} 或 {@code /legacy/}。
     *
     * @return Spring MVC 重定向视图名
     */
    @Since("3.2.0")
    @GetMapping("/")
    public String index() {
        String defaultUi = EnvUtil.getProperty(PROPERTY_DEFAULT_UI, UI_NEXT);
        return UI_LEGACY.equals(defaultUi) ? "redirect:/legacy/" : "redirect:/next/";
    }
    
    /** 新版 UI 入口，内部转发到 {@code /next/index.html} */
    @Since("3.2.0")
    @GetMapping("/next/")
    public String next() {
        return "forward:/next/index.html";
    }
    
    /** 旧版 UI 入口，内部转发到 {@code /legacy/index.html} */
    @Since("3.2.0")
    @GetMapping("/legacy/")
    public String legacy() {
        return "forward:/legacy/index.html";
    }
}
