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

package com.alibaba.nacos.client.config.utils;

import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.utils.LogUtils;
import org.slf4j.Logger;

/**
 * JVM 级客户端运行参数读取工具。
 *
 * <p>从 {@link NacosClientProperties} 读取多实例部署标志，供本地快照写盘等场景选择并发策略。</p>
 *
 * @author Nacos
 */
public class JvmUtil {
    
    /**
     * 当前 JVM 是否处于多实例部署模式。
     *
     * @return 多实例时为 true
     */
    public static Boolean isMultiInstance() {
        return isMultiInstance;
    }
    
    /** 多实例部署标志，由静态块从客户端属性初始化。 */
    private static Boolean isMultiInstance = false;
    
    /** 属性值为 true 时的字符串常量。 */
    private static final String TRUE = "true";
    
    private static final Logger LOGGER = LogUtils.logger(JvmUtil.class);
    
    /** 多实例部署属性键名。 */
    private static final String IS_MULTI_INSTANCE_PROPERTY = "isMultiInstance";
    
    /** 多实例部署默认值。 */
    private static final String DEFAULT_IS_MULTI_INSTANCE = "false";
    
    static {
        init();
    }
    
    /** 从客户端属性加载多实例部署配置。 */
    private static void init() {
        String multiDeploy = NacosClientProperties.PROTOTYPE
            .getProperty(IS_MULTI_INSTANCE_PROPERTY, DEFAULT_IS_MULTI_INSTANCE);
        if (TRUE.equals(multiDeploy)) {
            isMultiInstance = true;
        }
        LOGGER.info("isMultiInstance:{}", isMultiInstance);
    }
}
