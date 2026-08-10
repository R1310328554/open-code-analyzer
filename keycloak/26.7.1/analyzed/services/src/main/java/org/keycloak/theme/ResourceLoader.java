package org.keycloak.theme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * 主题资源加载工具。
 * <p>在规范化根路径下安全解析 classpath 与文件系统资源，防止路径穿越。</p>
 */
public class ResourceLoader {

    /** 从 classpath 根目录加载资源流，拒绝越界路径。 */
    public static InputStream getResourceAsStream(String root, String resource) throws IOException {
        if (root == null || resource == null) {
            return null;
        }
        Path rootPath = Path.of("/", root).normalize().toAbsolutePath();
        Path resourcePath = rootPath.resolve(resource).normalize().toAbsolutePath();
        if (resourcePath.startsWith(rootPath)) {
            if (File.separatorChar == '/') {
                resource = resourcePath.toString().substring(1);
            } else {
                resource = resourcePath.toString().substring(2).replace('\\', '/');
            }
            URL url = classLoader().getResource(resource);
            return url != null ? url.openStream() : null;
        } else {
            return null;
        }
    }

    /** 在文件根目录下打开资源输入流。 */
    public static InputStream getFileAsStream(File root, String resource) throws IOException {
        File file = getFile(root, resource);
        return file != null && file.isFile() ? file.toURI().toURL().openStream() : null;
    }

    /** 在文件根目录下解析资源文件，越界时返回 null。 */
    public static File getFile(File root, String resource) throws IOException {
        if (root == null || resource == null) {
            return null;
        }
        Path rootPath = root.toPath().toAbsolutePath().normalize();
        Path resourcePath = rootPath.resolve(resource).normalize().toAbsolutePath();
        if (resourcePath.startsWith(rootPath)) {
            return resourcePath.toFile();
        } else {
            return null;
        }
    }

    /** 当前线程上下文 ClassLoader。 */
    private static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
