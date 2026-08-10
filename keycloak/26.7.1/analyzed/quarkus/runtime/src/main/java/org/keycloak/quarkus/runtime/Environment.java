/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.keycloak.common.Profile;
import org.keycloak.common.util.NetworkUtils;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import io.quarkus.runtime.LaunchMode;
import io.smallrye.config.SmallRyeConfig;

/**
 * Keycloak Quarkus 运行时环境工具：主目录、Profile、启动模式与 Provider 路径等。
 */
public final class Environment {

    /** 系统属性：是否在容器中运行。 */
    public static final String KC_RUN_IN_CONTAINER = "KC_RUN_IN_CONTAINER";
    /** 系统属性：是否执行配置重建检查。 */
    public static final String KC_CONFIG_REBUILD_CHECK = "kc.config.rebuild-check";
    /** 系统属性：启动脚本进程 ID。 */
    public static final String KC_SCRIPT_PID = "kc.script.pid";
    /** 系统属性：配置是否已在构建时固化。 */
    public static final String KC_CONFIG_BUILT = "kc.config.built";
    /** 系统属性：Keycloak 安装主目录。 */
    public static final String KC_HOME_DIR = "kc.home.dir";
    /** 系统属性：Keycloak Profile 名称（kc.profile）。 */
    public static final String PROFILE ="kc.profile";
    /** 环境变量名：KC_PROFILE。 */
    public static final String ENV_PROFILE ="KC_PROFILE";
    /** 数据目录相对路径片段。 */
    public static final String DATA_PATH = File.separator + "data";
    /** 默认主题目录相对路径片段。 */
    public static final String DEFAULT_THEMES_PATH = File.separator +  "themes";
    /** 生产 Profile 常量值。 */
    public static final String PROD_PROFILE_VALUE = "prod";
    /** 系统属性：特殊启动模式标识。 */
    public static final String LAUNCH_MODE = "kc.launch.mode";
    /** 启动后立即退出模式。 */
    public static final String LAUNCH_MODE_EXIT_AFTER_START = "exit_after_start";
    /** 引导完成前退出模式。 */
    public static final String LAUNCH_MODE_EXIT_BEFORE_BOOTSTRAP = "exit_before_bootstrap";

    private Environment() {}

    /** @return 是否为 Quarkus 重新增强构建 */
    public static Boolean isRebuild() {
        return Boolean.getBoolean("quarkus.launch.rebuild");
    }

    /** @return Keycloak 主目录路径 */
    public static Optional<String> getHomeDir() {
        return Optional.ofNullable(System.getProperty(KC_HOME_DIR));
    }

    /** @return Keycloak 主目录 {@link Path} */
    public static Optional<Path> getHomePath() {
        return getHomeDir().map(Paths::get);
    }

    /** @return 数据目录路径 */
    public static Optional<String> getDataDir() {
        return getHomeDir().map(p -> p.concat(DATA_PATH));
    }

    /** @return 默认主题根目录 */
    public static Optional<String> getDefaultThemeRootDir() {
        return getHomeDir().map(p -> p.concat(DEFAULT_THEMES_PATH));
    }

    /** @return providers 扩展目录路径 */
    public static Optional<Path> getProvidersPath() {
        return Environment.getHomePath().map(p -> p.resolve("providers"));
    }

    /** @return 平台对应的启动命令（kc.sh 或 kc.bat） */
    public static String getCommand() {
        if (isWindows()) {
            return "kc.bat";
        }

        return "kc.sh";
    }

    /** 设置 Quarkus 与 Keycloak Profile 系统属性。 */
    public static void setProfile(String profile) {
        System.setProperty(org.keycloak.common.util.Environment.PROFILE, profile);
        System.setProperty(LaunchMode.current().getProfileKey(), profile);
        System.setProperty(SmallRyeConfig.SMALLRYE_CONFIG_PROFILE, profile);
    }

    /**
     * Check if the we're currently in or built as dev mode.
 * 判断当前是否处于或按开发模式构建。

     */
    public static boolean isDevMode() {
        if (org.keycloak.common.util.Environment.isDevMode()) {
            return true;
        }

        return org.keycloak.common.util.Environment.DEV_PROFILE_VALUE.equals(Configuration.getNonPersistedConfigValue(org.keycloak.common.util.Environment.PROFILE).getValue());
    }

    /** @return 是否为开发 Profile */
    public static boolean isDevProfile(){
        return org.keycloak.common.util.Environment.isDevMode();
    }

    /** @return 是否运行在 Windows 平台 */
    public static boolean isWindows() {
        return NetworkUtils.checkForWindows();
    }

    /** 强制切换为开发 Profile。 */
    public static void forceDevProfile() {
        setProfile(org.keycloak.common.util.Environment.DEV_PROFILE_VALUE);
    }

    /** @return providers 目录下 JAR 文件名到文件的映射 */
    public static Map<String, File> getProviderFiles() {
        Path providersPath = Environment.getProvidersPath().orElse(null);

        if (providersPath == null || !Files.exists(providersPath)) {
            return Collections.emptyMap();
        }

        File providersDir = providersPath.toFile();

        if (!providersDir.exists() || !providersDir.isDirectory()) {
            throw new RuntimeException("The 'providers' directory does not exist or is not a valid directory.");
        }

        return Arrays.stream(providersDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        })).collect(Collectors.toMap(File::getName, Function.identity()));
    }

    /** @return 是否配置了启动后/引导前早退模式 */
    public static boolean hasEarlyExitLaunchMode() {
        String mode = System.getProperty(LAUNCH_MODE);
        return LAUNCH_MODE_EXIT_AFTER_START.equals(mode) || LAUNCH_MODE_EXIT_BEFORE_BOOTSTRAP.equals(mode);
    }

    /** 强制启用启动后立即退出模式。 */
    public static void forceExitAfterStartLaunchMode() {
        System.setProperty(LAUNCH_MODE, LAUNCH_MODE_EXIT_AFTER_START);
    }

    /**
     * We want to hide the "profiles" from Quarkus to not make things unnecessarily complicated for users,
 * 将内部 Profile 映射为用户友好的启动模式名称，避免暴露 Quarkus Profile 细节。

     * so this method returns the equivalent launch mode instead. For use in e.g. CLI Output.
     *
     * @param profile the internal profile string used
     * @return the mapped launch mode, none when nothing is given or the profile as is when its
     * neither null/empty nor matching the quarkus default profiles we use.
     */
    public static String getKeycloakModeFromProfile(String profile) {

        if(profile == null || profile.isEmpty()) {
            return "none";
        }

        if(profile.equals(LaunchMode.DEVELOPMENT.getDefaultProfile())) {
            return "development";
        }

        if(profile.equals(LaunchMode.TEST.getDefaultProfile())) {
            return "test";
        }

        if(profile.equals(LaunchMode.NORMAL.getDefaultProfile())) {
            return "production";
        }

        //when no profile is matched and not empty, just return the profile name.
        return profile;
    }

    /** @return 是否启用配置重建检查 */
    public static boolean isRebuildCheck() {
        return Boolean.getBoolean(KC_CONFIG_REBUILD_CHECK);
    }

    /** 设置配置重建检查开关。 */
    public static void setRebuildCheck(boolean check) {
        System.setProperty(KC_CONFIG_REBUILD_CHECK, Boolean.toString(check));
    }

    /** @return 配置是否已在构建阶段固化 */
    public static boolean isRebuilt() {
        return Boolean.getBoolean(KC_CONFIG_BUILT);
    }

    /** 设置 Keycloak 主目录系统属性。 */
    public static void setHomeDir(Path path) {
        System.setProperty(KC_HOME_DIR, path.toFile().getAbsolutePath());
    }

    /**
     * Do not call this method at runtime.</p>
 * 请勿在运行时调用；构建步骤并行执行，方法为 synchronized。

     *
     * The method is marked as {@code synchronized} because build steps are executed in parallel.
     *
     * @return the current feature profile instance
     */
    public synchronized static Profile getCurrentOrCreateFeatureProfile() {
        Profile profile = Profile.getInstance();

        if (profile == null) {
            profile = Profile.configure(new QuarkusSingleProfileConfigResolver(), new QuarkusProfileConfigResolver());
        }

        return profile;
    }

    /** 标记当前为 Quarkus 重新增强构建。 */
    public static void setRebuild() {
        System.setProperty("quarkus.launch.rebuild", "true");
    }
    
    /**
     * The process id of the script used to launch the server. Will be null if a script other than kc.sh is used
 * 启动服务器的脚本进程 ID；若非 kc.sh/kc.bat 启动则为空。

     */
    public static Optional<String> getScriptPid() {
        return Optional.ofNullable(System.getProperty(KC_SCRIPT_PID));
    }
    
    /** @return 是否在容器环境中运行 */
    public static boolean isRunInContainer() {
        return Configuration.getOptionalBooleanKcValue("run-in-container").orElse(false);
    }
}
