/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.apache.httpclient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sentinel Apache HttpClient 适配器 Spring Boot 演示入口。
 *
 * @author zhaoyuguang
 */
@SpringBootApplication
public class ApacheHttpClientDemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ApacheHttpClientDemoApplication.class);
    }

    /** 启动后无额外逻辑，HTTP 测试由 Controller 接口触发。 */
    @Override
    public void run(String... args) {
    }
}
