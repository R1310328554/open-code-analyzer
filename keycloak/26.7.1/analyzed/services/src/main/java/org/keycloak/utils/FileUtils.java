package org.keycloak.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.keycloak.services.clientpolicy.ClientPoliciesUtil;

/**
 * 配置文件读取工具。
 * <p>优先从 classpath 加载 JSON，否则从 JBoss 配置目录读取。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FileUtils {

    /**
     * 从 classpath 或 JBoss 配置目录读取 JSON 文件输入流。
     *
     * @param fileName 不含路径的文件名
     * @return 文件输入流
     * @throws IOException 文件不存在或不可读时抛出
     */
    public static InputStream getJsonFileFromClasspathOrConfFolder(String fileName) throws IOException {
        // 优先从 classpath 读取 JSON 配置
        InputStream is = ClientPoliciesUtil.class.getResourceAsStream("/" + fileName);
        if (is == null) {
            Path path = Paths.get(System.getProperty("jboss.server.config.dir")).resolve(fileName);
            if (!Files.isReadable(path)) {
                throw new IOException(String.format("File \"%s\" does not exists under the config folder", path));
            }
            is = Files.newInputStream(path);
        }
        return is;
    }
}
