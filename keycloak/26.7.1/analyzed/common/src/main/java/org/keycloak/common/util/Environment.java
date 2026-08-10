/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.util;

import java.security.Provider;
import java.security.Security;

/**
 * 运行环境与 JVM 特性检测工具。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class Environment {

    /** 是否为 IBM JDK。 */
    public static final boolean IS_IBM_JAVA = System.getProperty("java.vendor").contains("IBM");

    /** JBoss/WildFly 默认启动超时（秒）。 */
    public static final int DEFAULT_JBOSS_AS_STARTUP_TIMEOUT = 300;

    /** Keycloak 配置文件属性名。 */
    public static final String PROFILE = "kc.profile";
    /** Keycloak 配置环境变量名。 */
    public static final String ENV_PROFILE = "KC_PROFILE";
    /** 开发模式 profile 值。 */
    public static final String DEV_PROFILE_VALUE = "dev";
    /** 非服务器模式 profile 值。 */
    public static final String NON_SERVER_MODE = "nonserver";

    /** 读取 JBoss 管理阻塞超时，未设置则返回默认值。 */
    public static int getServerStartupTimeout() {
        String timeout = System.getProperty("jboss.as.management.blocking.timeout");
        if (timeout != null) {
            return Integer.parseInt(timeout);
        } else {
            return DEFAULT_JBOSS_AS_STARTUP_TIMEOUT;
        }
    }

    /**
     * 检测 JVM 是否处于 FIPS 模式。
     * @return true if java is FIPS mode
     */
    public static boolean isJavaInFipsMode() {
        // 先检查 com.redhat.fips 系统属性
        String property = System.getProperty("com.redhat.fips");
        if (property != null) {
            return Boolean.parseBoolean(property);
        }

        // 否则自动检测 Security Provider 名称
        for (Provider provider : Security.getProviders()) {
            if (provider.getName().equals("BCFIPS")) {
                continue; // 忽略程序注册的 BCFIPS，避免误判
            }
            if (provider.getName().toUpperCase().contains("FIPS")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否为开发模式；非服务器命令可能在运行时切换，参见 {@link #isNonServerMode()}。
     */
    public static boolean isDevMode() {
        return DEV_PROFILE_VALUE.equalsIgnoreCase(getProfile());
    }

    /** 从系统属性或环境变量读取 Keycloak profile。 */
    public static String getProfile() {
        String profile = System.getProperty(PROFILE);

        if (profile != null) {
            return profile;
        }

        return System.getenv(ENV_PROFILE);
    }

    /**
     * 是否为非服务器模式（仅运行时有效；构建时按 prod/dev 区分）。
     */
    public static boolean isNonServerMode() {
        return NON_SERVER_MODE.equalsIgnoreCase(Environment.getProfile());
    }

}
