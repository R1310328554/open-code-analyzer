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

package com.alibaba.nacos.naming.consistency;

import com.alibaba.nacos.naming.misc.UtilsAndCommons;

/**
 * 命名一致性存储键构建工具。
 *
 * <p>统一生成服务元数据、开关域等记录在一致性层使用的键名。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
public class KeyBuilder {
    
    /** 命名空间与服务名之间的连接符。 */
    public static final String NAMESPACE_KEY_CONNECTOR = "##";
    
    /** 服务元数据键前缀。 */
    public static final String SERVICE_META_KEY_PREFIX = "com.alibaba.nacos.naming.domains.meta.";
    
    /** 构建指定命名空间下服务的元数据存储键。 */
    public static String buildServiceMetaKey(String namespaceId, String serviceName) {
        return SERVICE_META_KEY_PREFIX + namespaceId + NAMESPACE_KEY_CONNECTOR + serviceName;
    }
    
    /** 返回全局 {@link com.alibaba.nacos.naming.misc.SwitchDomain} 的存储键。 */
    public static String getSwitchDomainKey() {
        return SERVICE_META_KEY_PREFIX + UtilsAndCommons.SWITCH_DOMAIN_NAME;
    }
    
    /** 判断键是否为开关域相关键。 */
    public static boolean matchSwitchKey(String key) {
        return key.endsWith(UtilsAndCommons.SWITCH_DOMAIN_NAME);
    }
}
