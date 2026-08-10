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

package com.alibaba.nacos.core.listener.startup;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.event.ServerConfigChangeEvent;
import com.alibaba.nacos.common.executor.ThreadPoolManager;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.exception.ErrorCode;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.alibaba.nacos.sys.file.FileChangeEvent;
import com.alibaba.nacos.sys.file.FileWatcher;
import com.alibaba.nacos.sys.file.WatchFileCenter;
import com.alibaba.nacos.sys.utils.ApplicationUtils;
import com.alibaba.nacos.sys.utils.DiskUtils;
import com.alibaba.nacos.sys.utils.InetUtils;
import org.slf4j.Logger;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos 核心模块启动阶段：创建工作目录、加载配置、初始化系统属性与集群/存储模式。
 * <p>对应 {@link NacosStartUp#CORE_START_UP_PHASE}，是服务端最先执行的基础启动逻辑。</p>
 * Nacos Server Core start up phase.
 *
 * @author xiweng.yy
 */
public class NacosCoreStartUp extends AbstractNacosStartUp {
    
    /** JVM 系统属性：单机/集群模式标识。 */
    private static final String MODE_PROPERTY_KEY_STAND_MODE = "nacos.mode";
    
    /** JVM 系统属性：功能模式（config/naming/microservice/ai 等）。 */
    private static final String MODE_PROPERTY_KEY_FUNCTION_MODE = "nacos.function.mode";
    
    /** JVM 系统属性：本机 IP 地址。 */
    private static final String LOCAL_IP_PROPERTY_KEY = "nacos.local.ip";
    
    /** Spring PropertySource 名称：预加载的 application 配置。 */
    private static final String NACOS_APPLICATION_CONF = "nacos_application_conf";
    
    private static final String NACOS_MODE_STAND_ALONE = "stand alone";
    
    private static final String NACOS_MODE_CLUSTER = "cluster";
    
    private static final String DEFAULT_FUNCTION_MODE = "All";
    
    private static final String DATASOURCE_PLATFORM_PROPERTY = "spring.sql.init.platform";
    
    private static final String DERBY_DATABASE = "derby";
    
    private static final String DEFAULT_DATASOURCE_PLATFORM = "";
    
    private static final String DATASOURCE_MODE_EXTERNAL = "external";
    
    private static final String DATASOURCE_MODE_EMBEDDED = "embedded";
    
    /** 预加载配置缓存，供文件变更监听热更新。 */
    private static final Map<String, Object> SOURCES = new ConcurrentHashMap<>();
    
    /** 注册为核心启动阶段实现。 */
    public NacosCoreStartUp() {
        super(NacosStartUp.CORE_START_UP_PHASE);
    }
    
    /** 在 Nacos Home 下创建 logs、conf、data 工作目录。 */
    @Override
    public String[] makeWorkDir() {
        String[] dirNames = new String[] {"logs", "conf", "data"};
        List<String> result = new ArrayList<>(dirNames.length);
        for (String dirName : dirNames) {
            try {
                Path path = Paths.get(EnvUtil.getNacosHome(), dirName);
                DiskUtils.forceMkdir(new File(path.toUri()));
                result.add(path.toString());
            } catch (Exception e) {
                throw new NacosRuntimeException(ErrorCode.IOMakeDirError.getCode(), e);
            }
        }
        return result.toArray(new String[0]);
    }
    
    /** 将 Spring Environment 注入 {@link EnvUtil} 全局工具。 */
    @Override
    public void injectEnvironment(ConfigurableEnvironment environment) {
        EnvUtil.setEnvironment(environment);
    }
    
    /** 预加载 application 配置到 Environment 并注册 conf 目录文件监听。 */
    @Override
    public void loadPreProperties(ConfigurableEnvironment environment) {
        try {
            SOURCES.putAll(EnvUtil.loadProperties(EnvUtil.getApplicationConfFileResource()));
            environment.getPropertySources()
                .addLast(new OriginTrackedMapPropertySource(NACOS_APPLICATION_CONF, SOURCES));
            registerWatcher();
        } catch (Exception e) {
            throw new NacosRuntimeException(NacosException.SERVER_ERROR, e);
        }
    }
    
    /** 根据运行模式设置 nacos.mode、nacos.function.mode、nacos.local.ip 等系统属性。 */
    @Override
    public void initSystemProperty() {
        if (EnvUtil.getStandaloneMode()) {
            System.setProperty(MODE_PROPERTY_KEY_STAND_MODE, NACOS_MODE_STAND_ALONE);
        } else {
            System.setProperty(MODE_PROPERTY_KEY_STAND_MODE, NACOS_MODE_CLUSTER);
        }
        if (EnvUtil.getFunctionMode() == null) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, DEFAULT_FUNCTION_MODE);
        } else if (EnvUtil.FUNCTION_MODE_CONFIG.equals(EnvUtil.getFunctionMode())) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, EnvUtil.FUNCTION_MODE_CONFIG);
        } else if (EnvUtil.FUNCTION_MODE_NAMING.equals(EnvUtil.getFunctionMode())) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, EnvUtil.FUNCTION_MODE_NAMING);
        } else if (EnvUtil.FUNCTION_MODE_MICROSERVICE.equals(EnvUtil.getFunctionMode())) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, EnvUtil.FUNCTION_MODE_MICROSERVICE);
        } else if (EnvUtil.FUNCTION_MODE_AI.equals(EnvUtil.getFunctionMode())) {
            System.setProperty(MODE_PROPERTY_KEY_FUNCTION_MODE, EnvUtil.FUNCTION_MODE_AI);
        }
        
        System.setProperty(LOCAL_IP_PROPERTY_KEY, InetUtils.getSelfIP());
    }
    
    /** 集群模式下额外打印成员 IP 列表，再调用基类周期性启动日志。 */
    @Override
    public void logStartingInfo(Logger logger) {
        logClusterConf(logger);
        super.logStartingInfo(logger);
    }
    
    /** 调用 {@link EnvUtil#customEnvironment()} 应用自定义环境插件。 */
    @Override
    public void customEnvironment() {
        EnvUtil.customEnvironment();
    }
    
    /** 标记应用已启动完成（{@link ApplicationUtils#setStarted}）。 */
    @Override
    public void started() {
        super.started();
        ApplicationUtils.setStarted(true);
    }
    
    /** 启动日志阶段名："Nacos Server"。 */
    @Override
    protected String getPhaseNameInStartingInfo() {
        return "Nacos Server";
    }
    
    /** 输出启动成功日志：模式、存储类型（embedded/external）与耗时。 */
    @Override
    public void logStarted(Logger logger) {
        long endTimestamp = System.currentTimeMillis();
        long startupCost = endTimestamp - getStartTimestamp();
        boolean useExternalStorage = judgeStorageMode(EnvUtil.getEnvironment());
        logger.info("Nacos started successfully in {} mode with {} storage in {} ms",
            System.getProperty(MODE_PROPERTY_KEY_STAND_MODE),
            useExternalStorage ? DATASOURCE_MODE_EXTERNAL : DATASOURCE_MODE_EMBEDDED, startupCost);
    }
    
    /** 失败时额外关闭线程池、文件监听与事件总线。 */
    @Override
    public void failed(Throwable exception, ConfigurableApplicationContext context) {
        super.failed(exception, context);
        ThreadPoolManager.shutdown();
        WatchFileCenter.shutdown();
        NotifyCenter.shutdown();
    }
    
    /** 监听 conf 下 application.properties 变更并发布 {@link ServerConfigChangeEvent}。 */
    private void registerWatcher() throws NacosException {
        WatchFileCenter.registerWatcher(EnvUtil.getConfPath(), new FileWatcher() {
            
            @Override
            public void onChange(FileChangeEvent event) {
                try {
                    Map<String, ?> tmp =
                        EnvUtil.loadProperties(EnvUtil.getApplicationConfFileResource());
                    SOURCES.putAll(tmp);
                    NotifyCenter.publishEvent(ServerConfigChangeEvent.newEvent());
                } catch (IOException ignore) {
                }
            }
            
            @Override
            public boolean interest(String context) {
                return StringUtils.contains(context, "application.properties");
            }
        });
    }
    
    /** 非单机模式下打印 cluster.conf 中的成员 IP 列表。 */
    private void logClusterConf(Logger logger) {
        if (!EnvUtil.getStandaloneMode()) {
            try {
                List<String> clusterConf = EnvUtil.readClusterConf();
                logger.info("The server IP list of Nacos is {}", clusterConf);
            } catch (IOException e) {
                logger.error("read cluster conf fail", e);
            }
        }
    }
    
    /** 判断使用外部数据库还是内嵌 Derby（结合 standalone 与 embeddedStorage 开关）。 */
    private boolean judgeStorageMode(ConfigurableEnvironment env) {
        
        // 集群模式默认倾向外部数据源；Derby 或空 platform 视为内嵌
        String platform = this.getDatasourcePlatform(env);
        boolean useExternalStorage =
            !DEFAULT_DATASOURCE_PLATFORM.equalsIgnoreCase(platform)
                && !DERBY_DATABASE.equalsIgnoreCase(platform);
        
        // 须在 setUseExternalDB 之后初始化；standalone 默认内嵌，cluster 可开分布式存储
        // This value is true in stand-alone mode and false in cluster mode
        // If this value is set to true in cluster mode, nacos's distributed storage engine is turned on
        // default value is depend on ${nacos.standalone}
        
        if (!useExternalStorage) {
            boolean embeddedStorage =
                EnvUtil.getStandaloneMode() || Boolean.getBoolean("embeddedStorage");
            // 未显式开启内嵌存储时，自动升级为外部数据源（与历史行为一致）
            // upgraded to the external data source storage, as before
            if (!embeddedStorage) {
                useExternalStorage = true;
            }
        }
        return useExternalStorage;
    }
    
    /** 读取 spring.sql.init.platform 配置项。 */
    private String getDatasourcePlatform(ConfigurableEnvironment env) {
        return env.getProperty(DATASOURCE_PLATFORM_PROPERTY, DEFAULT_DATASOURCE_PLATFORM);
    }
}
