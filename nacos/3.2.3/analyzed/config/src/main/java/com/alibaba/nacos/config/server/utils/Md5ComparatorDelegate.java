/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.model.ConfigListenState;
import com.alibaba.nacos.sys.env.EnvUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * MD5 比对器委托单例：通过 SPI 加载 {@link Md5Comparator} 实现，按 {@code nacos.config.cache.type} 选择，未匹配时回退 {@link NacosMd5Comparator}。
 * The type Md5 comparator delegate.
 *
 * @author Sunrisea
 */
public class Md5ComparatorDelegate {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Md5ComparatorDelegate.class);
    
    /** 全局单例 */
    private static final Md5ComparatorDelegate INSTANCE = new Md5ComparatorDelegate();
    
    /** 配置项 nacos.config.cache.type 指定的比对器类型，默认 nacos */
    private String md5ComparatorType = EnvUtil.getProperty("nacos.config.cache.type", "nacos");
    
    /** 当前生效的 MD5 比对器实现 */
    private Md5Comparator md5Comparator;
    
    /** 构造时 SPI 扫描并绑定匹配的 Md5Comparator */
    private Md5ComparatorDelegate() {
        Collection<Md5Comparator> md5Comparators = NacosServiceLoader.load(Md5Comparator.class);
        for (Md5Comparator each : md5Comparators) {
            if (StringUtils.isEmpty(each.getName())) {
                LOGGER.warn(
                    "[Md5ComparatorDelegate] Load Md5Comparator({}) Md5ComparatorName(null/empty) fail. Please add Md5ComparatorName to resolve",
                    each.getClass().getName());
                continue;
            }
            LOGGER.info(
                "[Md5ComparatorDelegate] Load Md5Comparator({}) Md5ComparatorName({}) successfully.",
                each.getClass().getName(), each.getName());
            if (StringUtils.equals(md5ComparatorType, each.getName())) {
                LOGGER.info(
                    "[Md5ComparatorDelegate] Matched Md5Comparator found,set md5Comparator={}",
                    each.getClass().getName());
                md5Comparator = each;
            }
        }
        if (md5Comparator == null) {
            LOGGER.info(
                "[Md5ComparatorDelegate] Matched Md5Comparator not found, load Default NacosMd5Comparator successfully");
            md5Comparator = new NacosMd5Comparator();
        }
    }
    
    /** 获取委托单例 */
    public static Md5ComparatorDelegate getInstance() {
        return INSTANCE;
    }
    
    /** 委托当前比对器执行 MD5 差异检测 */
    public Map<String, ConfigListenState> compareMd5(HttpServletRequest request,
        HttpServletResponse response,
        Map<String, ConfigListenState> clientMd5Map) {
        return md5Comparator.compareMd5(request, response, clientMd5Map);
    }
}
