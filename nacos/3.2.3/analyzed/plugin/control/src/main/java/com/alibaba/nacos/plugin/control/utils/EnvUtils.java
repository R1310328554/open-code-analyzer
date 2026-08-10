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

package com.alibaba.nacos.plugin.control.utils;

import com.alibaba.nacos.common.utils.StringUtils;

import java.nio.file.Paths;

/**
 * 管控插件环境变量与路径工具。
 *
 * <p>解析 Nacos 安装根目录，优先读取系统属性 {@link #NACOS_HOME_KEY}，
 * 未配置时回退至 {@code user.home/nacos}。</p>
 *
 * @author xiweng.yy
 */
public class EnvUtils {
    
    /** 系统属性键：Nacos 安装根目录。 */
    public static final String NACOS_HOME_KEY = "nacos.home";
    
    /** 回退路径使用的用户主目录属性键。 */
    private static final String NACOS_HOME_PROPERTY = "user.home";
    
    /** 默认子目录名。 */
    private static final String NACOS_HOME_ADDITIONAL_FILEPATH = "nacos";
    
    /** 缓存的 Nacos 根目录路径。 */
    private static String nacosHomePath = null;
    
    /**
     * 获取 Nacos 安装根目录路径。
     *
     * <p>优先使用 {@code nacos.home} 系统属性；未设置时使用 {@code user.home/nacos}。</p>
     *
     * @return Nacos 根目录绝对路径
     */
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
        return nacosHomePath;
    }
}
