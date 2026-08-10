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

package com.alibaba.nacos.naming.misc;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.selector.SelectorType;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.VersionUtils;
import com.alibaba.nacos.naming.selector.LabelSelector;
import com.alibaba.nacos.naming.selector.NoneSelector;
import com.alibaba.nacos.sys.env.Constants;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 命名模块公共常量与工具方法集合。
 *
 * <p>定义 HTTP 路径前缀、开关域名、实例元数据键、Raft 数据目录等；并提供 metadata 解析与一致性哈希负载均衡 {@link #shakeUp} 等辅助能力。</p>
 *
 * @author nacos
 * @author jifengnan
 */
public class UtilsAndCommons {
    
    // ********************** Nacos HTTP 上下文路径常量 ************************ \\
    
    public static final String NACOS_SERVER_CONTEXT = "/nacos";
    
    public static final String NACOS_SERVER_VERSION = "/v1";
    
    public static final String NACOS_SERVER_VERSION_2 = "/v2";
    
    public static final String NACOS_SERVER_VERSION_3 = "/v3";
    
    public static final String DEFAULT_NACOS_NAMING_CONTEXT = NACOS_SERVER_VERSION + "/ns";
    
    public static final String DEFAULT_NACOS_NAMING_CONTEXT_V2 = NACOS_SERVER_VERSION_2 + "/ns";
    
    public static final String DEFAULT_NACOS_NAMING_ADMIN_CONTEXT_V3 =
        NACOS_SERVER_VERSION_3 + "/admin/ns";
    
    public static final String NACOS_NAMING_CONTEXT = DEFAULT_NACOS_NAMING_CONTEXT;
    
    public static final String NACOS_NAMING_CATALOG_CONTEXT = "/catalog";
    
    public static final String NACOS_NAMING_INSTANCE_CONTEXT = "/instance";
    
    public static final String NACOS_NAMING_SERVICE_CONTEXT = "/service";
    
    public static final String NACOS_NAMING_CLUSTER_CONTEXT = "/cluster";
    
    public static final String NACOS_NAMING_HEALTH_CONTEXT = "/health";
    
    public static final String NACOS_NAMING_CLIENT_CONTEXT = "/client";
    
    public static final String NACOS_NAMING_OPERATOR_CONTEXT = "/operator";
    
    public static final String NACOS_NAMING_OPERATOR_ADMIN_CONTEXT_V3 = "/ops";
    
    public static final String SERVICE_CONTROLLER_V3_ADMIN_PATH =
        DEFAULT_NACOS_NAMING_ADMIN_CONTEXT_V3 + NACOS_NAMING_SERVICE_CONTEXT;
    
    public static final String INSTANCE_CONTROLLER_V3_ADMIN_PATH =
        DEFAULT_NACOS_NAMING_ADMIN_CONTEXT_V3 + NACOS_NAMING_INSTANCE_CONTEXT;
    
    public static final String CLUSTER_CONTROLLER_V3_ADMIN_PATH =
        DEFAULT_NACOS_NAMING_ADMIN_CONTEXT_V3 + NACOS_NAMING_CLUSTER_CONTEXT;
    
    public static final String HEALTH_CONTROLLER_V3_ADMIN_PATH =
        DEFAULT_NACOS_NAMING_ADMIN_CONTEXT_V3 + NACOS_NAMING_HEALTH_CONTEXT;
    
    public static final String OPERATOR_CONTROLLER_V3_ADMIN_PATH =
        DEFAULT_NACOS_NAMING_ADMIN_CONTEXT_V3 + NACOS_NAMING_OPERATOR_ADMIN_CONTEXT_V3;
    
    public static final String CLIENT_CONTROLLER_V3_ADMIN_PATH =
        DEFAULT_NACOS_NAMING_ADMIN_CONTEXT_V3 + NACOS_NAMING_CLIENT_CONTEXT;
    
    public static final String V3_CLIENT_API_PATH = NACOS_SERVER_VERSION_3 + "/client";
    
    public static final String INSTANCE_V3_CLIENT_API_PATH = V3_CLIENT_API_PATH + "/ns/instance";
    
    // ********************** Nacos HTTP 上下文路径常量 ************************ //
    
    public static final String NACOS_SERVER_HEADER = Constants.NACOS_SERVER_HEADER;
    
    public static final String NACOS_VERSION = VersionUtils.version;
    
    public static final String SWITCH_DOMAIN_NAME = "00-00---000-NACOS_SWITCH_DOMAIN-000---00-00";
    
    public static final String CIDR_REGEX =
        "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}/[0-9]+";
    
    public static final String UNKNOWN_SITE = "unknown";
    
    public static final String DEFAULT_CLUSTER_NAME = "DEFAULT";
    
    public static final String LOCALHOST_SITE = UtilsAndCommons.UNKNOWN_SITE;
    
    public static final String SERVER_VERSION = NACOS_SERVER_HEADER + ":" + NACOS_VERSION;
    
    public static final String SELF_SERVICE_CLUSTER_ENV = "naming_self_service_cluster_ips";
    
    public static final String CACHE_KEY_SPLITTER = "@@@@";
    
    public static final int MAX_PUBLISH_WAIT_TIME_MILLIS = 5000;
    
    public static final String VERSION_STRING_SYNTAX = "[0-9]+\\.[0-9]+\\.[0-9]+";
    
    public static final String API_UPDATE_SWITCH = "/api/updateSwitch";
    
    public static final String API_SET_ALL_WEIGHTS = "/api/setWeight4AllIPs";
    
    public static final String API_DOM = "/api/dom";
    
    public static final String NAMESPACE_SERVICE_CONNECTOR = "##";
    
    public static final String UPDATE_INSTANCE_ACTION_ADD = "add";
    
    public static final String UPDATE_INSTANCE_ACTION_REMOVE = "remove";
    
    public static final String UPDATE_INSTANCE_METADATA_ACTION_UPDATE = "update";
    
    public static final String UPDATE_INSTANCE_METADATA_ACTION_REMOVE = "remove";
    
    public static final String EPHEMERAL = "ephemeral";
    
    public static final String PERSIST = "persist";
    
    public static final String DATA_BASE_DIR =
        EnvUtil.getNacosHome() + File.separator + "data" + File.separator + "naming";
    
    public static final String RAFT_CACHE_FILE_PREFIX = "com.alibaba.nacos.naming";
    
    public static final String NUMBER_PATTERN = "^\\d+$";
    
    public static final String ENABLE_HEALTH_CHECK = "enableHealthCheck";
    
    public static final String ENABLE_CLIENT_BEAT = "enableClientBeat";
    
    static {
        
        /*
            Register subType for serialization
        
            Now these subType implementation class has registered in static code.
            But there are some problem for classloader. The implementation class
            will be loaded when they are used, which will make deserialize
            before register.
        
            子类实现类中的静态代码串中已经向Jackson进行了注册，但是由于classloader的原因，只有当
            该子类被使用的时候，才会加载该类。这可能会导致Jackson先进性反序列化，再注册子类，从而导致
            反序列化失败。
         */
        // TODO register in implementation class or remove subType
        JacksonUtils.registerSubtype(NoneSelector.class, SelectorType.none.name());
        JacksonUtils.registerSubtype(LabelSelector.class, SelectorType.label.name());
        
    }
    
    /**
     * 将 metadata 字符串解析为键值 Map。
     *
     * <p>优先按 JSON 解析，失败则回退为逗号分隔的 key=value 格式。</p>
     *
     * @param metadata meta data string
     * @return meta data map
     * @throws NacosException nacos exception
     */
    public static Map<String, String> parseMetadata(String metadata) throws NacosException {
        
        Map<String, String> metadataMap = new HashMap<>(16);
        
        if (StringUtils.isBlank(metadata)) {
            return metadataMap;
        }
        
        try {
            metadataMap = JacksonUtils.toObj(metadata, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            String[] datas = metadata.split(",");
            if (datas.length > 0) {
                for (String data : datas) {
                    String[] kv = data.split("=");
                    if (kv.length != 2) {
                        throw new NacosApiException(HttpStatus.BAD_REQUEST.value(),
                            ErrorCode.INSTANCE_METADATA_ERROR,
                            "metadata format incorrect:" + metadata);
                    }
                    metadataMap.put(kv[0], kv[1]);
                }
            }
        }
        
        return metadataMap;
    }
    
    /** 拼接 namespaceId 与服务名，使用 {@link #NAMESPACE_SERVICE_CONNECTOR} 连接。 */
    public static String assembleFullServiceName(String namespaceId, String serviceName) {
        return namespaceId + UtilsAndCommons.NAMESPACE_SERVICE_CONNECTOR + serviceName;
    }
    
    /**
     * 基于字符串哈希在 [0, upperLimit) 区间生成近似均匀分布的索引。
     *
     * <p>常用于同一服务多节点 IP 列表的确定性负载均衡选择。</p>
     * <p>e.g. Assume there's an array which contains some IP of the servers provide the same service, the caller name
     * can be used to choose the server to achieve load balance.
     * <blockquote><pre>
     *     String[] serverIps = new String[10];
     *     int index = shakeUp("callerName", serverIps.length);
     *     String targetServerIp = serverIps[index];
     * </pre></blockquote></p>
     *
     * @param string     a string. the number 0 will be returned if it's null
     * @param upperLimit the upper limit of the returned number, must be a positive integer, which means > 0
     * @return a number between 0(inclusive) and upperLimit(exclusive)
     * @throws IllegalArgumentException if the upper limit equals or less than 0
     * @author jifengnan
     * @since 1.0.0
     */
    public static int shakeUp(String string, int upperLimit) {
        if (upperLimit < 1) {
            throw new IllegalArgumentException("upper limit must be greater than 0");
        }
        if (string == null) {
            return 0;
        }
        return (string.hashCode() & Integer.MAX_VALUE) % upperLimit;
    }
    
}
