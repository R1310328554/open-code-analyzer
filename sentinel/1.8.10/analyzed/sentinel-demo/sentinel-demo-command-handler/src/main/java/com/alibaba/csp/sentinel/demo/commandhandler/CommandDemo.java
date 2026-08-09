/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.commandhandler;

import com.alibaba.csp.sentinel.init.InitExecutor;

/**
 * <p>运行本演示需引入 {@code sentinel-transport-simple-http} 依赖。</p>
 * <p>
 * CommandCenter 初始化后可访问 {@code http://ip:commandPort/api} 查看内置命令 API（默认端口 8719）。
 * 亦可访问自定义 {@code /echo} 命令。
 * </p>
 *
 * @author Eric Zhao
 */
public class CommandDemo {

    public static void main(String[] args) {
        // 仅演示用；业务应用通常由 SPI 自动触发 InitFunc
        InitExecutor.doInit();

        System.out.println("Sentinel CommandCenter has been initialized");
    }
}
