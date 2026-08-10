/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.airegistry.config;

import org.apache.tomcat.util.buf.EncodedSolidusHandling;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * In the MCP community API definition, the MCP server name is passed as a path parameter.
 * Because the server name can contain '/', we need to configure Tomcat and WebSecurity
 * to allow '/' in the path.
 * <p>MCP 服务名可含 '/'，需配置 Tomcat 编码斜杠透传与 Spring Security 防火墙允许 URL 编码斜杠/百分号。</p>
 *
 * @author xinluo
 */
@Configuration
public class HttpPathConfiguration {
    
    /** 允许 Tomcat 对路径中编码斜杠透传处理。 */
    @Bean
    public TomcatConnectorCustomizer connectorCustomizer() {
        return (connector) -> connector
            .setEncodedSolidusHandling(EncodedSolidusHandling.PASS_THROUGH.getValue());
    }
    
    /** 放宽 Spring Security 防火墙以接受含编码斜杠的 MCP 服务名路径。 */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowUrlEncodedPercent(true);
        return (web) -> web.httpFirewall(firewall);
    }
}
