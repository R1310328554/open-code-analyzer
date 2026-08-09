/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.rocketmq.common.utils;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;

/**
 * NameServer 地址解析与云实例 Endpoint 校验工具。
 */
public class NameServerAddressUtils {
    /** 云实例 ID 前缀，如 MQ_INST_xxx_yyy。 */
    public static final String INSTANCE_PREFIX = "MQ_INST_";
    /** 实例 ID 正则片段。 */
    public static final String INSTANCE_REGEX = INSTANCE_PREFIX + "\\w+_\\w+";
    /** Endpoint 可选协议前缀捕获组。 */
    public static final String ENDPOINT_PREFIX = "(\\w+://|)";
    /** HTTP NameServer Endpoint 模式。 */
    public static final Pattern NAMESRV_ENDPOINT_PATTERN = Pattern.compile("^http://.*");
    /** 云实例 Endpoint 模式（可选协议 + 实例 ID + 域名后缀）。 */
    public static final Pattern INST_ENDPOINT_PATTERN = Pattern.compile("^" + ENDPOINT_PREFIX + INSTANCE_REGEX + "\\..*");

    /** 从系统属性或环境变量读取 NameServer 地址列表。 */
    public static String getNameServerAddresses() {
        return System.getProperty(MixAll.NAMESRV_ADDR_PROPERTY, System.getenv(MixAll.NAMESRV_ADDR_ENV));
    }

    /** 校验字符串是否符合云实例 Endpoint 格式。 */
    public static boolean validateInstanceEndpoint(String endpoint) {
        return INST_ENDPOINT_PATTERN.matcher(endpoint).matches();
    }

    /**
     * 从 Endpoint URL 解析实例 ID（最后一个 '/' 与第一个 '.' 之间的片段）。
     *
     * @param endpoint 完整 Endpoint，空则返回 null
     */
    public static String parseInstanceIdFromEndpoint(String endpoint) {
        if (StringUtils.isEmpty(endpoint)) {
            return null;
        }
        return endpoint.substring(endpoint.lastIndexOf("/") + 1, endpoint.indexOf('.'));
    }

    /**
     * 从 NameServer HTTP Endpoint 提取 host:port 地址部分。
     *
     * @param nameSrvEndpoint 完整 Endpoint，空则返回 null
     */
    public static String getNameSrvAddrFromNamesrvEndpoint(String nameSrvEndpoint) {
        if (StringUtils.isEmpty(nameSrvEndpoint)) {
            return null;
        }
        return nameSrvEndpoint.substring(nameSrvEndpoint.lastIndexOf('/') + 1);
    }
}
