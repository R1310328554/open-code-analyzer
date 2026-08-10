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

package com.alibaba.nacos.bootstrap;

import com.alibaba.nacos.airegistry.NacosAiRegistry;
import com.alibaba.nacos.NacosServerBasicApplication;
import com.alibaba.nacos.NacosServerWebApplication;
import com.alibaba.nacos.console.NacosConsole;
import com.alibaba.nacos.core.listener.startup.NacosStartUp;
import com.alibaba.nacos.core.listener.startup.NacosStartUpManager;
import com.alibaba.nacos.sys.env.Constants;
import com.alibaba.nacos.sys.env.DeploymentType;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.RegistrationPolicy;

/**
 * Nacos 服务端启动引导类。
 *
 * <p>根据 {@link DeploymentType} 部署模式启动 Core、Web、Console 与 AI Registry
 * 等 Spring Boot 子上下文，支持合并部署、仅服务端与仅控制台三种形态。</p>
 *
 * @author xiweng.yy
 */
@SpringBootApplication
public class NacosBootstrap {
    
    /** Spring JMX 开关配置键。 */
    private static final String SPRING_JMX_ENABLED = "spring.jmx.enabled";
    
    /** 程序入口：解析部署类型并按模式启动对应 Spring 上下文组合。 */
    public static void main(String[] args) {
        String type = System.getProperty(Constants.NACOS_DEPLOYMENT_TYPE,
            Constants.NACOS_DEPLOYMENT_TYPE_MERGED);
        DeploymentType deploymentType = DeploymentType.getType(type);
        EnvUtil.setDeploymentType(deploymentType);
        switch (deploymentType) {
            case MERGED:
                startWithConsole(args);
                break;
            case SERVER:
                startWithoutConsole(args);
                break;
            case CONSOLE:
                startOnlyConsole(args);
                break;
            default:
                throw new IllegalArgumentException("Unsupported nacos deployment type " + type);
        }
    }
    
    /** 准备 Core 上下文：若启用 JMX 则设置 MBean 注册策略为忽略已存在项。 */
    private static void prepareCoreContext(ConfigurableApplicationContext coreContext) {
        if (coreContext.getEnvironment().getProperty(SPRING_JMX_ENABLED, Boolean.class, false)) {
            // 避免 MBean 重复注册到 exporter。
            coreContext.getBean(MBeanExporter.class)
                .setRegistrationPolicy(RegistrationPolicy.IGNORE_EXISTING);
        }
    }
    
    /** 仅启动 Core + Web（及可选 AI Registry），不加载 Console。 */
    private static void startWithoutConsole(String[] args) {
        ConfigurableApplicationContext coreContext = startCoreContext(args);
        prepareCoreContext(coreContext);
        ConfigurableApplicationContext webContext = startServerWebContext(args, coreContext);
        if (isEnabledAiRegistry(coreContext)) {
            ConfigurableApplicationContext aiRegistryContext =
                startAiRegistryContext(args, coreContext);
        }
    }
    
    /** 合并部署：启动 Core、Web、Console 及可选 AI Registry。 */
    private static void startWithConsole(String[] args) {
        ConfigurableApplicationContext coreContext = startCoreContext(args);
        prepareCoreContext(coreContext);
        ConfigurableApplicationContext serverWebContext = startServerWebContext(args, coreContext);
        ConfigurableApplicationContext consoleContext = startConsoleContext(args, coreContext);
        if (isEnabledAiRegistry(coreContext)) {
            ConfigurableApplicationContext aiRegistryContext =
                startAiRegistryContext(args, coreContext);
        }
    }
    
    /** 启动 Core 基础上下文（无 Web 容器）。 */
    private static ConfigurableApplicationContext startCoreContext(String[] args) {
        NacosStartUpManager.start(NacosStartUp.CORE_START_UP_PHASE);
        return new SpringApplicationBuilder(NacosServerBasicApplication.class)
            .web(WebApplicationType.NONE)
            .banner(getBanner("core-banner.txt")).run(args);
    }
    
    /** 以 Core 为父上下文启动服务端 Web 应用。 */
    private static ConfigurableApplicationContext startServerWebContext(String[] args,
        ConfigurableApplicationContext coreContext) {
        NacosStartUpManager.start(NacosStartUp.WEB_START_UP_PHASE);
        return new SpringApplicationBuilder(NacosServerWebApplication.class).parent(coreContext)
            .banner(getBanner("nacos-server-web-banner.txt")).run(args);
    }
    
    /** 以 Core 为父上下文启动控制台应用。 */
    private static ConfigurableApplicationContext startConsoleContext(String[] args,
        ConfigurableApplicationContext coreContext) {
        NacosStartUpManager.start(NacosStartUp.CONSOLE_START_UP_PHASE);
        return new SpringApplicationBuilder(NacosConsole.class).parent(coreContext)
            .banner(getBanner("nacos-console-banner.txt")).run(args);
    }
    
    /** 以 Core 为父上下文启动 AI Registry 应用。 */
    private static ConfigurableApplicationContext startAiRegistryContext(String[] args,
        ConfigurableApplicationContext coreContext) {
        NacosStartUpManager.start(NacosStartUp.AI_REGISTRY_START_UP_PHASE);
        return new SpringApplicationBuilder(NacosAiRegistry.class).parent(coreContext)
            .banner(getBanner("nacos-ai-registry-banner.txt")).run(args);
    }
    
    /** 独立控制台部署：仅启动 Console 上下文。 */
    private static void startOnlyConsole(String[] args) {
        NacosStartUpManager.start(NacosStartUp.CONSOLE_START_UP_PHASE);
        new SpringApplicationBuilder(NacosConsole.class).banner(
            getBanner("nacos-console-banner.txt")).run(args);
    }
    
    /** 从类路径加载指定 Banner 文件。 */
    private static Banner getBanner(String bannerFileName) {
        return new ResourceBanner(new ClassPathResource(bannerFileName));
    }
    
    /** 判断 MCP 或 Skill 注册中心是否至少有一项启用。 */
    private static boolean isEnabledAiRegistry(ConfigurableApplicationContext coreContext) {
        boolean mcpRegistryEnabled = coreContext.getEnvironment()
            .getProperty("nacos.ai.mcp.registry.enabled", Boolean.class, false);
        boolean skillRegistryEnabled = coreContext.getEnvironment()
            .getProperty("nacos.ai.skill.registry.enabled", Boolean.class, false);
        return mcpRegistryEnabled || skillRegistryEnabled;
    }
}
