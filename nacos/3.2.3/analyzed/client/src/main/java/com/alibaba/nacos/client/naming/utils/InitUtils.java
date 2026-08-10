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

package com.alibaba.nacos.client.naming.utils;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.SystemPropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.selector.ExpressionSelector;
import com.alibaba.nacos.api.selector.NoneSelector;
import com.alibaba.nacos.api.selector.SelectorType;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.env.SourceType;
import com.alibaba.nacos.client.utils.ContextPathUtil;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.client.utils.TemplateUtils;
import com.alibaba.nacos.client.utils.TenantUtil;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * 命名客户端初始化工具。
 *
 * <p>负责解析 namespace、Web 上下文路径及 Jackson 选择器子类型注册等启动期配置。</p>
 *
 * @author liaochuntao
 * @author deshao
 */
public class InitUtils {
    
    /**
     * 为命名模块解析 namespace（与 Config 初始化逻辑不同，不可直接复用）。
     *
     * <p>依次尝试云环境 ANS、ALIWARE 环境变量、JVM 与配置文件，最终回退 {@link UtilAndComs#DEFAULT_NAMESPACE_ID}。</p>
     *
     * @param properties 客户端配置
     * @return 解析后的 namespace ID
     */
    public static String initNamespaceForNaming(NacosClientProperties properties) {
        String tmpNamespace = null;
        
        String isUseCloudNamespaceParsing =
            properties.getProperty(PropertyKeyConst.IS_USE_CLOUD_NAMESPACE_PARSING,
                properties.getProperty(
                    SystemPropertyKeyConst.IS_USE_CLOUD_NAMESPACE_PARSING,
                    String.valueOf(Constants.DEFAULT_USE_CLOUD_NAMESPACE_PARSING)));
        
        if (Boolean.parseBoolean(isUseCloudNamespaceParsing)) {
            
            tmpNamespace = TenantUtil.getUserTenantForAns();
            LogUtils.NAMING_LOGGER.info("initializer namespace from ans.namespace attribute : {}",
                tmpNamespace);
            
            tmpNamespace = TemplateUtils.stringEmptyAndThenExecute(tmpNamespace, () -> {
                String namespace = properties
                    .getProperty(PropertyKeyConst.SystemEnv.ALIBABA_ALIWARE_NAMESPACE);
                LogUtils.NAMING_LOGGER.info(
                    "initializer namespace from ALIBABA_ALIWARE_NAMESPACE attribute :"
                        + namespace);
                return namespace;
            });
        }
        
        tmpNamespace = TemplateUtils.stringEmptyAndThenExecute(tmpNamespace, () -> {
            String namespace =
                properties.getPropertyFrom(SourceType.JVM, PropertyKeyConst.NAMESPACE);
            LogUtils.NAMING_LOGGER
                .info("initializer namespace from namespace attribute :" + namespace);
            return namespace;
        });
        
        if (StringUtils.isEmpty(tmpNamespace)) {
            tmpNamespace = properties.getProperty(PropertyKeyConst.NAMESPACE);
        }
        
        tmpNamespace = TemplateUtils.stringEmptyAndThenExecute(tmpNamespace,
            () -> UtilAndComs.DEFAULT_NAMESPACE_ID);
        return tmpNamespace;
    }
    
    /**
     * 根据 {@link PropertyKeyConst#CONTEXT_PATH} 初始化 Web 根路径与命名 HTTP URL 常量。
     *
     * @param properties 客户端配置
     * @since 1.4.1
     */
    public static void initWebRootContext(NacosClientProperties properties) {
        final String webContext = properties.getProperty(PropertyKeyConst.CONTEXT_PATH);
        TemplateUtils.stringNotEmptyAndThenExecute(webContext, () -> {
            UtilAndComs.webContext = ContextPathUtil.normalizeContextPath(webContext);
            UtilAndComs.nacosUrlBase = UtilAndComs.webContext + "/v1/ns";
            UtilAndComs.nacosUrlInstance = UtilAndComs.nacosUrlBase + "/instance";
        });
    }
    
    /**
     * 预注册 Jackson 选择器子类型，避免 classloader 延迟加载导致反序列化失败。
     *
     * <p>实现类静态块中虽已注册，但若子类尚未加载，Jackson 可能先反序列化后注册而失败。</p>
     */
    public static void initSerialization() {
        // TODO：考虑在实现类中注册或移除 subType 机制
        JacksonUtils.registerSubtype(NoneSelector.class, SelectorType.none.name());
        JacksonUtils.registerSubtype(ExpressionSelector.class, SelectorType.label.name());
    }
}
