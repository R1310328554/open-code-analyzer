/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Nacos 控制台 Spring Boot 启动入口：加载 console 配置、启用定时任务并启动 Web 容器。
 * Nacos console starter.
 *
 * @author xiweng.yy
 */
@SpringBootApplication(exclude = LdapAutoConfiguration.class)
@PropertySource("classpath:nacos-console.properties")
@EnableScheduling
public class NacosConsole {
    
    /**
     * 控制台进程主入口，委托 {@link SpringApplication} 启动 Spring 上下文。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NacosConsole.class, args);
    }
}
