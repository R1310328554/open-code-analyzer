/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.zuul.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Zuul 1.x + Spring Cloud + Sentinel 网关演示入口。
 * <p>连接 Dashboard 示例参数：</p>
 * <code>
 * -Dproject.name=zuul-gateway -Dcsp.sentinel.dashboard.server=localhost:8080
 * -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.app.type=1
 * </code>
 *
 * @author Eric Zhao
 */
@SpringBootApplication
@EnableZuulProxy
public class ZuulGatewayDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayDemoApplication.class, args);
    }
}
