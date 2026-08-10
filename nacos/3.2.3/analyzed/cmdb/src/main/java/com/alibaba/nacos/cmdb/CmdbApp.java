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

package com.alibaba.nacos.cmdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CMDB starter.
 * <p>Nacos CMDB（配置管理数据库）模块 Spring Boot 启动入口，加载 CMDB 内存提供者、定时同步任务与运维 HTTP 接口。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
@SpringBootApplication
public class CmdbApp {
    
    /** JVM 入口：启动 CMDB 独立进程或嵌入 Nacos 时的 CMDB 子应用 */
    public static void main(String[] args) {
        SpringApplication.run(CmdbApp.class, args);
    }
}
