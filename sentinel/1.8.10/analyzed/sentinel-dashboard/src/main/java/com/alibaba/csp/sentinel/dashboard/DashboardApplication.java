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
package com.alibaba.csp.sentinel.dashboard;

import com.alibaba.csp.sentinel.init.InitExecutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sentinel 控制台 Spring Boot 启动入口。
 * <p>启动前在独立线程中触发 {@link com.alibaba.csp.sentinel.init.InitExecutor} 完成 Sentinel 初始化。</p>
 *
 * @author Carpenter Lee
 */
@SpringBootApplication
public class DashboardApplication {

    /** 应用主入口：先异步初始化 Sentinel，再启动 Spring Boot。 */
    public static void main(String[] args) {
        triggerSentinelInit();
        SpringApplication.run(DashboardApplication.class, args);
    }

    /** 在后台线程执行 Sentinel 初始化，避免阻塞 Spring 启动。 */
    private static void triggerSentinelInit() {
        new Thread(() -> InitExecutor.doInit()).start();
    }
}
