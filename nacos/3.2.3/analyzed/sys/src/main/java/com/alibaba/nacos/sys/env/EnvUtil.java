/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.nacos.sys.env;

import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.plugin.environment.CustomEnvironmentPluginManager;
import com.alibaba.nacos.sys.utils.DiskUtils;
import com.alibaba.nacos.sys.utils.InetUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.HashMap;

/**
 * Nacos 运行时环境与配置访问工具类。
 *
 * <p>封装 Spring {@link ConfigurableEnvironment}、单机/集群模式、Nacos Home、集群成员列表及系统资源指标等全局信息的读取与缓存。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class EnvUtil {
    
    public static final String STANDALONE_MODE_ALONE = "standalone";
    
    public static final String STANDALONE_MODE_CLUSTER = "cluster";
    
    public static final String FUNCTION_MODE_CONFIG = "config";
    
    public static final String FUNCTION_MODE_NAMING = "naming";
    
    public static final String FUNCTION_MODE_MICROSERVICE = "microservice";
    
    public static final String FUNCTION_MODE_AI = "ai";
    
    /** 系统属性键：Nacos 安装根目录（{@code nacos.home}）。 */
    public static final String NACOS_HOME_KEY = "nacos.home";
    
    private static volatile String localAddress = "";
    
    private static int port = -1;
    
    private static Boolean isStandalone = null;
    
    private static String functionModeType = null;
    
    private static String contextPath = null;
    
    private static final String FILE_PREFIX = "file:";
    
    private static final String SERVER_PORT_PROPERTY = "nacos.server.main.port";
    
    private static final int DEFAULT_SERVER_PORT = 8848;
    
    private static final String DEFAULT_WEB_CONTEXT_PATH = "/nacos";
    
    private static final String MEMBER_LIST_PROPERTY = "nacos.member.list";
    
    private static final String NACOS_HOME_PROPERTY = "user.home";
    
    private static final String CUSTOM_CONFIG_LOCATION_PROPERTY =
        "spring.config.additional-location";
    
    private static final String DEFAULT_CONFIG_LOCATION = "application.properties";
    
    private static final String DEFAULT_RESOURCE_PATH = "/application.properties";
    
    private static final String DEFAULT_ADDITIONAL_PATH = "conf";
    
    private static final String DEFAULT_ADDITIONAL_FILE = "cluster.conf";
    
    private static final String NACOS_HOME_ADDITIONAL_FILEPATH = "nacos";
    
    private static final String NACOS_TEMP_DIR_1 = "data";
    
    private static final String NACOS_TEMP_DIR_2 = "tmp";
    
    private static final String NACOS_CUSTOM_ENVIRONMENT_ENABLED =
        "nacos.custom.environment.enabled";
    
    private static final String NACOS_CUSTOM_CONFIG_NAME = "customFirstNacosConfig";
    
    @JustForTest
    private static String confPath = "";
    
    @JustForTest
    private static String nacosHomePath = null;
    
    private static ConfigurableEnvironment environment;
    
    private static DeploymentType deploymentType;
    
    /** 若启用自定义环境插件，将插件转换后的属性注入 Spring 环境首位。 */
    public static void customEnvironment() {
        boolean enableCustom = getProperty(NACOS_CUSTOM_ENVIRONMENT_ENABLED, Boolean.class, false);
        if (enableCustom) {
            Set<String> propertyKeys =
                CustomEnvironmentPluginManager.getInstance().getPropertyKeys();
            Map<String, Object> sourcePropertyMap = new HashMap<>(propertyKeys.size());
            for (String key : propertyKeys) {
                sourcePropertyMap.put(key, getProperty(key, Object.class));
            }
            Map<String, Object> targetMap = CustomEnvironmentPluginManager.getInstance()
                .getCustomValues(sourcePropertyMap);
            MutablePropertySources propertySources = environment.getPropertySources();
            propertySources.addFirst(new MapPropertySource(NACOS_CUSTOM_CONFIG_NAME, targetMap));
        }
    }
    
    public static ConfigurableEnvironment getEnvironment() {
        return environment;
    }
    
    public static void setEnvironment(ConfigurableEnvironment environment) {
        EnvUtil.environment = environment;
    }
    
    public static boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }
    
    public static String getProperty(String key) {
        return environment.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }
    
    public static <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }
    
    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return environment.getProperty(key, targetType, defaultValue);
    }
    
    public static String getRequiredProperty(String key) throws IllegalStateException {
        return environment.getRequiredProperty(key);
    }
    
    public static <T> T getRequiredProperty(String key, Class<T> targetType)
        throws IllegalStateException {
        return environment.getRequiredProperty(key, targetType);
    }
    
    public static Properties getProperties() {
        Properties properties = new Properties();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> enumerablePropertySource =
                    (EnumerablePropertySource<?>) propertySource;
                String[] propertyNames = enumerablePropertySource.getPropertyNames();
                for (String propertyName : propertyNames) {
                    Object propertyValue = enumerablePropertySource.getProperty(propertyName);
                    if (propertyValue != null) {
                        properties.put(propertyName, propertyValue.toString());
                    }
                }
            }
        }
        return properties;
    }
    
    public static String resolvePlaceholders(String text) {
        return environment.resolvePlaceholders(text);
    }
    
    public static String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return environment.resolveRequiredPlaceholders(text);
    }
    
    public static List<String> getPropertyList(String key) {
        List<String> valueList = new ArrayList<>();
        
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String value = environment.getProperty(key + "[" + i + "]");
            if (StringUtils.isBlank(value)) {
                break;
            }
            
            valueList.add(value);
        }
        
        return valueList;
    }
    
    public static String getLocalAddress() {
        if (StringUtils.isBlank(localAddress)) {
            localAddress = InetUtils.getSelfIP() + ":" + getPort();
        }
        return localAddress;
    }
    
    public static void setLocalAddress(String localAddress) {
        EnvUtil.localAddress = localAddress;
    }
    
    public static void systemExit() {
        System.exit(0);
    }
    
    public static int getPort() {
        if (port == -1) {
            port = getProperty(SERVER_PORT_PROPERTY, Integer.class, DEFAULT_SERVER_PORT);
        }
        return port;
    }
    
    public static void setPort(int port) {
        EnvUtil.port = port;
    }
    
    public static String getContextPath() {
        if (Objects.isNull(contextPath)) {
            contextPath = getProperty(Constants.WEB_CONTEXT_PATH, DEFAULT_WEB_CONTEXT_PATH);
            if (Constants.ROOT_WEB_CONTEXT_PATH.equals(contextPath)) {
                contextPath = StringUtils.EMPTY;
            }
        }
        return contextPath;
    }
    
    public static void setContextPath(String contextPath) {
        EnvUtil.contextPath = contextPath;
    }
    
    @JustForTest
    public static void setIsStandalone(Boolean isStandalone) {
        EnvUtil.isStandalone = isStandalone;
    }
    
    /** 是否单机模式（读取 {@link Constants#STANDALONE_MODE_PROPERTY_NAME}）。 */
    public static boolean getStandaloneMode() {
        if (Objects.isNull(isStandalone)) {
            isStandalone = Boolean.getBoolean(Constants.STANDALONE_MODE_PROPERTY_NAME);
        }
        return isStandalone;
    }
    
    /** 获取 Server 功能模式（config/naming/microservice/ai 等）。 */
    public static String getFunctionMode() {
        if (StringUtils.isEmpty(functionModeType)) {
            functionModeType = System.getProperty(Constants.FUNCTION_MODE_PROPERTY_NAME);
        }
        return functionModeType;
    }
    
    private static String nacosTmpDir;
    
    public static String getNacosTmpDir() {
        if (StringUtils.isBlank(nacosTmpDir)) {
            nacosTmpDir = Paths.get(getNacosHome(), NACOS_TEMP_DIR_1, NACOS_TEMP_DIR_2).toString();
        }
        return nacosTmpDir;
    }
    
    public static String getNacosHome() {
        if (StringUtils.isBlank(nacosHomePath)) {
            String nacosHome = System.getProperty(NACOS_HOME_KEY);
            if (StringUtils.isBlank(nacosHome)) {
                nacosHome = Paths
                    .get(System.getProperty(NACOS_HOME_PROPERTY), NACOS_HOME_ADDITIONAL_FILEPATH)
                    .toString();
            }
            return nacosHome;
        }
        // 测试场景优先返回注入的 nacosHomePath
        return nacosHomePath;
    }
    
    @JustForTest
    public static void setNacosHomePath(String nacosHomePath) {
        EnvUtil.nacosHomePath = nacosHomePath;
    }
    
    public static String getSystemEnv(String key) {
        return System.getenv(key);
    }
    
    public static float getLoad() {
        return (float) OperatingSystemBeanManager.getOperatingSystemBean().getSystemLoadAverage();
    }
    
    public static float getCpu() {
        return (float) OperatingSystemBeanManager.getSystemCpuUsage();
    }
    
    public static float getMem() {
        return (float) (1
            - (double) OperatingSystemBeanManager.getFreePhysicalMem()
                / (double) OperatingSystemBeanManager.getTotalPhysicalMem());
    }
    
    public static String getConfPath() {
        if (StringUtils.isNotBlank(EnvUtil.confPath)) {
            return EnvUtil.confPath;
        }
        EnvUtil.confPath = Paths.get(getNacosHome(), DEFAULT_ADDITIONAL_PATH).toString();
        return confPath;
    }
    
    public static void setConfPath(final String confPath) {
        EnvUtil.confPath = confPath;
    }
    
    public static String getClusterConfFilePath() {
        return Paths.get(getNacosHome(), DEFAULT_ADDITIONAL_PATH, DEFAULT_ADDITIONAL_FILE)
            .toString();
    }
    
    /**
     * 读取 cluster.conf 或环境变量中的集群成员 IP 列表。
     *
     * @return ip list.
     * @throws IOException ioexception {@link IOException}
     */
    public static List<String> readClusterConf() throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(getClusterConfFilePath()),
            StandardCharsets.UTF_8)) {
            return analyzeClusterConf(reader);
        } catch (FileNotFoundException ignore) {
            List<String> tmp = new ArrayList<>();
            String clusters = EnvUtil.getMemberList();
            if (StringUtils.isNotBlank(clusters)) {
                String[] details = clusters.split(",");
                for (String item : details) {
                    tmp.add(item.trim());
                }
            }
            return tmp;
        }
    }
    
    /**
     * 解析 cluster.conf 文本流为成员地址列表（支持注释与逗号分隔）。
     *
     * @param reader reader
     * @return ip list.
     * @throws IOException IOException
     */
    public static List<String> analyzeClusterConf(Reader reader) throws IOException {
        List<String> instanceList = new ArrayList<String>();
        List<String> lines = IoUtils.readLines(reader);
        String comment = "#";
        for (String line : lines) {
            String instance = line.trim();
            if (instance.startsWith(comment)) {
                // 以 # 开头的行视为注释，跳过
                continue;
            }
            if (instance.contains(comment)) {
                // 行内 # 之后为注释，截取有效地址部分
                instance = instance.substring(0, instance.indexOf(comment));
                instance = instance.trim();
            }
            int multiIndex = instance.indexOf(Constants.COMMA_DIVISION);
            if (multiIndex > 0) {
                // 支持同一行逗号分隔多个 ip:port
                instanceList.addAll(Arrays.asList(instance.split(Constants.COMMA_DIVISION)));
            } else {
                // 支持单行单地址格式 ip:port
                instanceList.add(instance);
            }
        }
        return instanceList;
    }
    
    public static void writeClusterConf(String content) throws IOException {
        DiskUtils.writeFile(new File(getClusterConfFilePath()),
            content.getBytes(StandardCharsets.UTF_8), false);
    }
    
    public static String getMemberList() {
        String val;
        if (environment == null) {
            val = System.getenv(MEMBER_LIST_PROPERTY);
            if (StringUtils.isBlank(val)) {
                val = System.getProperty(MEMBER_LIST_PROPERTY);
            }
        } else {
            val = getProperty(MEMBER_LIST_PROPERTY);
        }
        return val;
    }
    
    /**
     * 通过 {@link OriginTrackedPropertiesLoader} 加载 properties 资源为 Map。
     *
     * @param resource resource
     * @return Map&lt;String, Object&gt;
     * @throws IOException IOException
     */
    public static Map<String, ?> loadProperties(Resource resource) throws IOException {
        return new OriginTrackedPropertiesLoader(resource).load();
    }
    
    public static Resource getApplicationConfFileResource() {
        Resource customResource = getCustomFileResource();
        return customResource == null ? getDefaultResource() : customResource;
    }
    
    private static Resource getCustomFileResource() {
        String path = getProperty(CUSTOM_CONFIG_LOCATION_PROPERTY);
        if (StringUtils.isNotBlank(path) && path.contains(FILE_PREFIX)) {
            String[] paths = path.split(",", -1);
            path = paths[paths.length - 1].substring(FILE_PREFIX.length());
            return getRelativePathResource(path, DEFAULT_CONFIG_LOCATION);
        }
        return null;
    }
    
    private static Resource getRelativePathResource(String parentPath, String path) {
        try {
            InputStream inputStream = new FileInputStream(Paths.get(parentPath, path).toFile());
            return new InputStreamResource(inputStream);
        } catch (Exception ignore) {
        }
        return null;
    }
    
    private static Resource getDefaultResource() {
        InputStream inputStream = EnvUtil.class.getResourceAsStream(DEFAULT_RESOURCE_PATH);
        return new InputStreamResource(inputStream);
    }
    
    /**
     * 从环境配置获取可用 CPU 核心数。
     *
     * <p>优先读取 {@code nacos.core.sys.basic.processors}，未配置时使用 {@code ThreadUtils.getSuitableThreadCount()}，结果不低于 1。</p>
     *
     * @return available processor numbers from environment, will not lower than 1.
     */
    public static int getAvailableProcessors() {
        int result = getProperty(Constants.AVAILABLE_PROCESSORS_BASIC, int.class,
            ThreadUtils.getSuitableThreadCount(1));
        return result > 0 ? result : 1;
    }
    
    /**
     * 获取可用核心数的整数倍（用于线程池容量估算）。
     *
     * @param multiple multiple of available processor numbers
     * @return available processor numbers from environment, will not lower than 1.
     */
    public static int getAvailableProcessors(int multiple) {
        if (multiple < 1) {
            throw new IllegalArgumentException("processors multiple must upper than 1");
        }
        Integer processor = getProperty(Constants.AVAILABLE_PROCESSORS_BASIC, Integer.class);
        return null != processor && processor > 0 ? processor * multiple
            : ThreadUtils.getSuitableThreadCount(multiple);
    }
    
    /**
     * 按 0～1 比例缩放可用核心数。
     *
     * @param scale scale from 0 to 1.
     * @return available processor numbers from environment, will not lower than 1.
     */
    public static int getAvailableProcessors(double scale) {
        if (scale < 0 || scale > 1) {
            throw new IllegalArgumentException("processors scale must between 0 and 1");
        }
        double result =
            getProperty(Constants.AVAILABLE_PROCESSORS_BASIC, int.class,
                ThreadUtils.getSuitableThreadCount(1))
                * scale;
        return result > 1 ? (int) result : 1;
    }
    
    public static DeploymentType getDeploymentType() {
        return deploymentType;
    }
    
    public static void setDeploymentType(DeploymentType deploymentType) {
        EnvUtil.deploymentType = deploymentType;
    }
}
