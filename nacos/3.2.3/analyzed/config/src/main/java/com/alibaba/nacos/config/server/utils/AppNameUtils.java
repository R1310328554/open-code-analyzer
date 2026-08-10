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

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.common.utils.StringUtils;

import java.io.File;

/**
 * 应用名推断工具：依次尝试 {@code project.name} 系统属性与常见应用服务器 home 路径，解析出 Nacos 客户端侧上报的应用标识。
 * appName util.
 *
 * @author Nacos
 */
public class AppNameUtils {
    
    /** JVM 启动参数 project.name */
    private static final String PARAM_MARKING_PROJECT = "project.name";
    
    private static final String PARAM_MARKING_JBOSS = "jboss.server.home.dir";
    
    private static final String PARAM_MARKING_JETTY = "jetty.home";
    
    private static final String PARAM_MARKING_TOMCAT = "catalina.base";
    
    /** 阿里云标准部署根路径前缀 */
    private static final String LINUX_ADMIN_HOME = "/home/admin/";
    
    private static final String SERVER_JBOSS = "jboss";
    
    private static final String SERVER_JETTY = "jetty";
    
    private static final String SERVER_TOMCAT = "tomcat";
    
    private static final String SERVER_UNKNOWN = "unknown server";
    
    /** 无法识别时的默认应用名 */
    private static final String DEFAULT_APP_NAME = "unknown";
    
    /** 获取当前进程应用名：优先 project.name，其次从 server home 路径解析 */
    public static String getAppName() {
        String appName;
        
        appName = getAppNameByProjectName();
        if (appName != null) {
            return appName;
        }
        
        appName = getAppNameByServerHome();
        if (appName != null) {
            return appName;
        }
        
        return DEFAULT_APP_NAME;
    }
    
    /** 从 project.name 系统属性读取应用名 */
    private static String getAppNameByProjectName() {
        return System.getProperty(PARAM_MARKING_PROJECT);
    }
    
    /** 从 JBoss/Jetty/Tomcat home 路径 /home/admin/{app}/ 段解析应用名 */
    private static String getAppNameByServerHome() {
        String serverHome = null;
        if (SERVER_JBOSS.equals(getServerType())) {
            serverHome = System.getProperty(PARAM_MARKING_JBOSS);
        } else if (SERVER_JETTY.equals(getServerType())) {
            serverHome = System.getProperty(PARAM_MARKING_JETTY);
        } else if (SERVER_TOMCAT.equals(getServerType())) {
            serverHome = System.getProperty(PARAM_MARKING_TOMCAT);
        }
        
        if (serverHome != null && serverHome.startsWith(LINUX_ADMIN_HOME)) {
            return StringUtils.substringBetween(serverHome, LINUX_ADMIN_HOME, File.separator);
        }
        
        return null;
    }
    
    /** 根据 JVM 属性判断当前运行的应用服务器类型 */
    private static String getServerType() {
        String serverType;
        if (System.getProperty(PARAM_MARKING_JBOSS) != null) {
            serverType = SERVER_JBOSS;
        } else if (System.getProperty(PARAM_MARKING_JETTY) != null) {
            serverType = SERVER_JETTY;
        } else if (System.getProperty(PARAM_MARKING_TOMCAT) != null) {
            serverType = SERVER_TOMCAT;
        } else {
            serverType = SERVER_UNKNOWN;
        }
        return serverType;
    }
    
}
