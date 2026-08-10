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

package com.alibaba.nacos.client.utils;

import com.alibaba.nacos.api.common.Constants;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * env util.
 * <p>客户端环境标签工具：从 HTTP 响应头解析并缓存本机 amory、vipserver、location 等路由标签，供服务发现与就近访问策略使用。</p>
 *
 * @author Nacos
 */
public class EnvUtil {
    
    /** 本类日志记录器 */
    public static final Logger LOGGER = LogUtils.logger(EnvUtil.class);
    
    /** 当前实例 amory 环境标签 */
    private static String selfAmoryTag;
    
    /** 当前实例 vipserver 标签 */
    private static String selfVipserverTag;
    
    /** 当前实例 location 地域/机房标签 */
    private static String selfLocationTag;
    
    /**
     * 根据响应头更新本机环境标签缓存。
     * <p>头缺失时清空对应标签；值变化时打 warn 日志便于排查路由漂移。</p>
     */
    public static void setSelfEnv(Map<String, List<String>> headers) {
        if (headers != null) {
            List<String> amoryTagTmp = headers.get(Constants.AMORY_TAG);
            if (amoryTagTmp == null) {
                if (selfAmoryTag != null) {
                    selfAmoryTag = null;
                    LOGGER.warn("selfAmoryTag:null");
                }
            } else {
                String amoryTagTmpStr = listToString(amoryTagTmp);
                if (!Objects.equals(amoryTagTmpStr, selfAmoryTag)) {
                    selfAmoryTag = amoryTagTmpStr;
                    LOGGER.warn("selfAmoryTag:{}", selfAmoryTag);
                }
            }
            
            List<String> vipserverTagTmp = headers.get(Constants.VIPSERVER_TAG);
            if (vipserverTagTmp == null) {
                if (selfVipserverTag != null) {
                    selfVipserverTag = null;
                    LOGGER.warn("selfVipserverTag:null");
                }
            } else {
                String vipserverTagTmpStr = listToString(vipserverTagTmp);
                if (!Objects.equals(vipserverTagTmpStr, selfVipserverTag)) {
                    selfVipserverTag = vipserverTagTmpStr;
                    LOGGER.warn("selfVipserverTag:{}", selfVipserverTag);
                }
            }
            List<String> locationTagTmp = headers.get(Constants.LOCATION_TAG);
            if (locationTagTmp == null) {
                if (selfLocationTag != null) {
                    selfLocationTag = null;
                    LOGGER.warn("selfLocationTag:null");
                }
            } else {
                String locationTagTmpStr = listToString(locationTagTmp);
                if (!Objects.equals(locationTagTmpStr, selfLocationTag)) {
                    selfLocationTag = locationTagTmpStr;
                    LOGGER.warn("selfLocationTag:{}", selfLocationTag);
                }
            }
        }
    }
    
    /** 返回缓存的 amory 标签 */
    public static String getSelfAmoryTag() {
        return selfAmoryTag;
    }
    
    /** 返回缓存的 vipserver 标签 */
    public static String getSelfVipserverTag() {
        return selfVipserverTag;
    }
    
    /** 返回缓存的 location 标签 */
    public static String getSelfLocationTag() {
        return selfLocationTag;
    }
    
    /** 将多值头列表用逗号拼接为单字符串 */
    private static String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String string : list) {
            result.append(string);
            result.append(',');
        }
        return result.substring(0, result.length() - 1);
    }
}
