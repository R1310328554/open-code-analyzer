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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.core.exception.ErrorCode;
import com.alibaba.nacos.core.listener.startup.AbstractNacosStartUp;
import com.alibaba.nacos.core.listener.startup.NacosStartUp;
import com.alibaba.nacos.sys.env.Constants;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.utils.DiskUtils;
import com.alibaba.nacos.sys.utils.InetUtils;
import org.slf4j.Logger;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制台启动阶段处理器：在独立部署（console 模式）下初始化工作目录、环境属性与系统变量。
 * Nacos Server Web API start up phase.
 *
 * @author xiweng.yy
 */
public class NacosConsoleStartUp extends AbstractNacosStartUp {
    
    /** 单机/集群运行模式对应的系统属性键 */
    private static final String MODE_PROPERTY_KEY_STAND_MODE = "nacos.mode";
    
    /** 功能模块裁剪模式对应的系统属性键 */
    private static final String MODE_PROPERTY_KEY_FUNCTION_MODE = "nacos.function.mode";
    
    /** 控制台独立部署时固定为单机模式 */
    private static final String NACOS_MODE_STAND_ALONE = "stand alone";
    
    /** 未显式指定 functionMode 时的默认值（启用全部模块） */
    private static final String DEFAULT_FUNCTION_MODE = "All";
    
    /** 本机 IP 写入系统属性的键名 */
    private static final String LOCAL_IP_PROPERTY_KEY = "nacos.local.ip";
    
    /** 预加载 application 配置在 Spring 环境中的 PropertySource 名称 */
    private static final String NACOS_APPLICATION_CONF = "nacos_application_conf";
    
    /** 从 application 配置文件加载的键值对缓存（console 独立部署时使用） */
    private static final Map<String, Object> SOURCES = new ConcurrentHashMap<>();
    
    /** 当前 JVM 是否以 console 独立部署类型启动 */
    private boolean isConsoleDeploymentType;
    
    /** 注册控制台启动阶段到 {@link NacosStartUp#CONSOLE_START_UP_PHASE} */
    public NacosConsoleStartUp() {
        super(NacosStartUp.CONSOLE_START_UP_PHASE);
    }
    
    /** {@inheritDoc} 返回启动日志中展示的阶段名称 */
    @Override
    protected String getPhaseNameInStartingInfo() {
        return "Nacos Console";
    }
    
    /**
     * 创建控制台运行所需工作目录；独立部署时在 Nacos Home 下确保 logs 目录存在。
     *
     * @return 工作目录路径数组
     */
    @Override
    public String[] makeWorkDir() {
        isConsoleDeploymentType = Constants.NACOS_DEPLOYMENT_TYPE_CONSOLE.equals(
            System.getProperty(Constants.NACOS_DEPLOYMENT_TYPE));
        if (isConsoleDeploymentType) {
            try {
                Path path = Paths.get(EnvUtil.getNacosHome(), "logs");
                DiskUtils.forceMkdir(new File(path.toUri()));
            } catch (Exception e) {
                throw new NacosRuntimeException(ErrorCode.IOMakeDirError.getCode(), e);
            }
            return new String[] {EnvUtil.getNacosHome() + File.separator + "logs"};
        }
        return super.makeWorkDir();
    }
    
    /**
     * 独立部署时将 Spring 环境注入 {@link EnvUtil}，供后续配置读取。
     *
     * @param environment 可配置的 Spring 环境
     */
    @Override
    public void injectEnvironment(ConfigurableEnvironment environment) {
        if (isConsoleDeploymentType) {
            EnvUtil.setEnvironment(environment);
        }
    }
    
    /**
     * 独立部署时预加载 application 配置文件并追加为最低优先级 PropertySource。
     *
     * @param environment 可配置的 Spring 环境
     */
    @Override
    public void loadPreProperties(ConfigurableEnvironment environment) {
        if (isConsoleDeploymentType) {
            try {
                SOURCES.putAll(EnvUtil.loadProperties(EnvUtil.getApplicationConfFileResource()));
                environment.getPropertySources()
                    .addLast(new OriginTrackedMapPropertySource(NACOS_APPLICATION_CONF, SOURCES));
            } catch (Exception e) {
                throw new NacosRuntimeException(NacosException.SERVER_ERROR, e);
            }
        }
    }
    
    /** 独立部署时写入本机 IP、单机模式及 functionMode 等系统属性 */
    @Override
    public void initSystemProperty() {
        if (isConsoleDeploymentType) {
            System.setProperty(LOCAL_IP_PROPERTY_KEY, InetUtils.getSelfIP());
            System.setProperty(MODE_PROPERTY_KEY_STAND_MODE, NACOS_MODE_STAND_ALONE);
            if (EnvUtil.getFunctionMode() == null) {
                System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, DEFAULT_FUNCTION_MODE);
            } else if (EnvUtil.FUNCTION_MODE_CONFIG.equals(EnvUtil.getFunctionMode())) {
                System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, EnvUtil.FUNCTION_MODE_CONFIG);
            } else if (EnvUtil.FUNCTION_MODE_NAMING.equals(EnvUtil.getFunctionMode())) {
                System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, EnvUtil.FUNCTION_MODE_NAMING);
            } else if (EnvUtil.FUNCTION_MODE_MICROSERVICE.equals(EnvUtil.getFunctionMode())) {
                System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE,
                    EnvUtil.FUNCTION_MODE_MICROSERVICE);
            } else if (EnvUtil.FUNCTION_MODE_AI.equals(EnvUtil.getFunctionMode())) {
                System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, EnvUtil.FUNCTION_MODE_AI);
            }
        }
    }
    
    /** 输出控制台启动耗时日志 */
    @Override
    public void logStarted(Logger logger) {
        long endTimestamp = System.currentTimeMillis();
        long startupCost = endTimestamp - getStartTimestamp();
        logger.info("Nacos Console started successfully in {} ms", startupCost);
    }
}
