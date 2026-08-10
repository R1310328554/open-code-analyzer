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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.List;

/**
 * 配置客户端请求参数校验工具。
 *
 * <p>对 dataId、group、tenant、content、betaIps 等字段做白名单字符校验，
 * 不合法时抛出 {@link NacosException#CLIENT_INVALID_PARAM}。</p>
 *
 * @author Nacos
 */
public class ParamUtils {
    
    /** 参数白名单允许的额外字符：下划线、连字符、点、冒号。 */
    private static final char[] VALID_CHARS = new char[] {'_', '-', '.', ':'};
    
    /** content 参数非法时的错误消息。 */
    private static final String CONTENT_INVALID_MSG = "content invalid";
    
    /** dataId 参数非法时的错误消息。 */
    private static final String DATAID_INVALID_MSG = "dataId invalid";
    
    /** tenant 参数非法时的错误消息。 */
    private static final String TENANT_INVALID_MSG = "tenant invalid";
    
    /** betaIps 参数非法时的错误消息。 */
    private static final String BETAIPS_INVALID_MSG = "betaIps invalid";
    
    /** group 参数非法时的错误消息。 */
    private static final String GROUP_INVALID_MSG = "group invalid";
    
    /** datumId 参数非法时的错误消息。 */
    private static final String DATUMID_INVALID_MSG = "datumId invalid";
    
    /**
     * 白名单校验：参数仅可包含字母、数字及 {@link #VALID_CHARS} 中的字符，且非空。
     *
     * @param param 待校验字符串
     * @return 合法返回 true
     */
    public static boolean isValid(String param) {
        if (param == null) {
            return false;
        }
        int length = param.length();
        for (int i = 0; i < length; i++) {
            char ch = param.charAt(i);
            if (!Character.isLetterOrDigit(ch) && !isValidChar(ch)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 判断字符是否在白名单额外字符集中。
     *
     * @param ch 待测字符
     * @return 在白名单中返回 true
     */
    private static boolean isValidChar(char ch) {
        for (char c : VALID_CHARS) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 校验 tenant、dataId 与 group。
     *
     * @param tenant 命名空间
     * @param dataId 配置 Data ID
     * @param group  配置分组
     * @throws NacosException 任一参数不合法时抛出
     */
    public static void checkTdg(String tenant, String dataId, String group) throws NacosException {
        checkTenant(tenant);
        if (StringUtils.isBlank(dataId) || !ParamUtils.isValid(dataId)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, DATAID_INVALID_MSG);
        }
        if (StringUtils.isBlank(group) || !ParamUtils.isValid(group)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, GROUP_INVALID_MSG);
        }
    }
    
    /**
     * 校验 dataId 与 group。
     *
     * @param dataId 配置 Data ID
     * @param group  配置分组
     * @throws NacosException 参数不合法时抛出
     */
    public static void checkKeyParam(String dataId, String group) throws NacosException {
        if (StringUtils.isBlank(dataId) || !ParamUtils.isValid(dataId)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, DATAID_INVALID_MSG);
        }
        if (StringUtils.isBlank(group) || !ParamUtils.isValid(group)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, GROUP_INVALID_MSG);
        }
    }
    
    /**
     * 校验 dataId、group 与 datumId。
     *
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param datumId 聚合配置 datum 标识
     * @throws NacosException 参数不合法时抛出
     */
    public static void checkKeyParam(String dataId, String group, String datumId)
        throws NacosException {
        if (StringUtils.isBlank(dataId) || !ParamUtils.isValid(dataId)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, DATAID_INVALID_MSG);
        }
        if (StringUtils.isBlank(group) || !ParamUtils.isValid(group)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, GROUP_INVALID_MSG);
        }
        if (StringUtils.isBlank(datumId) || !ParamUtils.isValid(datumId)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, DATUMID_INVALID_MSG);
        }
    }
    
    /**
     * 批量校验 dataId 列表与 group。
     *
     * @param dataIds 配置 Data ID 列表
     * @param group   配置分组
     * @throws NacosException 列表为空或任一 dataId/group 不合法时抛出
     */
    public static void checkKeyParam(List<String> dataIds, String group) throws NacosException {
        if (dataIds == null || dataIds.size() == 0) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "dataIds invalid");
        }
        for (String dataId : dataIds) {
            if (StringUtils.isBlank(dataId) || !ParamUtils.isValid(dataId)) {
                throw new NacosException(NacosException.CLIENT_INVALID_PARAM, DATAID_INVALID_MSG);
            }
        }
        if (StringUtils.isBlank(group) || !ParamUtils.isValid(group)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, GROUP_INVALID_MSG);
        }
    }
    
    /**
     * 校验 dataId、group 与 content。
     *
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param content 配置内容
     * @throws NacosException 参数不合法时抛出
     */
    public static void checkParam(String dataId, String group, String content)
        throws NacosException {
        checkKeyParam(dataId, group);
        if (StringUtils.isBlank(content)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, CONTENT_INVALID_MSG);
        }
    }
    
    /**
     * 校验 dataId、group、datumId 与 content。
     *
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param datumId 聚合配置 datum 标识
     * @param content 配置内容
     * @throws NacosException 参数不合法时抛出
     */
    public static void checkParam(String dataId, String group, String datumId, String content)
        throws NacosException {
        checkKeyParam(dataId, group, datumId);
        if (StringUtils.isBlank(content)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, CONTENT_INVALID_MSG);
        }
    }
    
    /**
     * 校验 tenant（命名空间）。
     *
     * @param tenant 命名空间标识
     * @throws NacosException tenant 为空或字符不合法时抛出
     */
    public static void checkTenant(String tenant) throws NacosException {
        if (StringUtils.isBlank(tenant) || !ParamUtils.isValid(tenant)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, TENANT_INVALID_MSG);
        }
    }
    
    /**
     * 校验 Beta 发布 IP 列表（逗号分隔）。
     *
     * @param betaIps Beta IP 列表字符串
     * @throws NacosException 为空或含非法 IP 时抛出
     */
    public static void checkBetaIps(String betaIps) throws NacosException {
        if (StringUtils.isBlank(betaIps)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, BETAIPS_INVALID_MSG);
        }
        String[] ipsArr = betaIps.split(",");
        for (String ip : ipsArr) {
            if (!InternetAddressUtil.isIp(ip)) {
                throw new NacosException(NacosException.CLIENT_INVALID_PARAM, BETAIPS_INVALID_MSG);
            }
        }
    }
    
    /**
     * 校验配置内容非空。
     *
     * @param content 配置内容
     * @throws NacosException content 为空时抛出
     */
    public static void checkContent(String content) throws NacosException {
        if (StringUtils.isBlank(content)) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, CONTENT_INVALID_MSG);
        }
    }
}
