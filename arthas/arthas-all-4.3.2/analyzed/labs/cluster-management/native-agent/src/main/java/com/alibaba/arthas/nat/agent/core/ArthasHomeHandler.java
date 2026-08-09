package com.alibaba.arthas.nat.agent.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Arthas 安装目录定位器：依次尝试 ~/.arthas/lib/arthas 与当前 JAR 所在目录。
 *
 * @description: find arthas home
 * @author：flzjkl
 * @date: 2024-07-27 9:12
 */
public class ArthasHomeHandler {

    private static final Logger logger = LoggerFactory.getLogger(ArthasHomeHandler.class);
    /** 解析成功后缓存的 Arthas 根目录 */
    public static File ARTHAS_HOME_DIR;

    /**
     * 查找并校验 Arthas 安装目录，要求包含 core/agent/spy 三个 JAR。
     */
    public static void findArthasHome() {
        // 优先从 ~/.arthas/lib/arthas 查找
        File arthasHomeDir = null;
        try {
            if (arthasHomeDir == null) {
                File arthasDir = new File(System.getProperty("user.home"), ".arthas" + File.separator + "lib"
                        + File.separator + "arthas");
                verifyArthasHome(arthasDir.getAbsolutePath());
                arthasHomeDir = arthasDir;
            }
        } catch (Exception e) {
            // 用户目录不存在有效安装，继续尝试其他路径
        }

        // 回退：以当前 JAR 所在目录作为 Arthas Home
        try {
            if (arthasHomeDir == null) {
                URL jarUrl = ArthasHomeHandler.class.getProtectionDomain().getCodeSource().getLocation();
                if (jarUrl != null) {
                    File arthasDir = new File(jarUrl.toURI());
                    // 若为 JAR 文件则取其父目录
                    String jarDir = arthasDir.getParent();
                    verifyArthasHome(jarDir);
                    if (arthasDir != null) {
                        arthasHomeDir = new File(jarDir);
                    }
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (arthasHomeDir == null) {
            logger.error("Please ensure that arthas-native agent-client is in the same directory as arthas-core.jar, arthas-agent.jar, and arthas-spy.jar");
            throw new RuntimeException("arthas home not found");
        }

        ARTHAS_HOME_DIR = arthasHomeDir;
    }

    /**
     * 校验目录存在且包含 arthas-core/agent/spy 三个必需 JAR。
     *
     * @param arthasHome 待校验目录路径
     */
    private static void verifyArthasHome(String arthasHome) {
        File home = new File(arthasHome);
        if (home.isDirectory()) {
            String[] fileList = {"arthas-core.jar", "arthas-agent.jar", "arthas-spy.jar"};

            for (String fileName : fileList) {
                if (!new File(home, fileName).exists()) {
                    logger.error("Please ensure that arthas-native agent-client is in the same directory as arthas-core.jar, arthas-agent.jar, and arthas-spy.jar");
                    throw new IllegalArgumentException(
                            fileName + " do not exist, arthas home: " + home.getAbsolutePath());
                }
            }
            return;
        }

        throw new IllegalArgumentException("illegal arthas home: " + home.getAbsolutePath());
    }
}
