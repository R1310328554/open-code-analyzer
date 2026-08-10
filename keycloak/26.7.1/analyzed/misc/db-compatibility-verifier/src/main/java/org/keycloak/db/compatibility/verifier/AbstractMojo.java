package org.keycloak.db.compatibility.verifier;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * DB 兼容性校验 Maven 插件的抽象基类。
 * 提供 JSON 序列化、项目 classpath 与通用配置参数。
 */
abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    /** 格式化输出的 Jackson 映射器，用于读写校验 JSON 文件 */
    final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /** 已支持数据库变更集列表 JSON 文件路径 */
    @Parameter(property = "db.verify.supportedFile", required = true)
    protected String supportedFile;

    /** 明确不支持的数据库变更集列表 JSON 文件路径 */
    @Parameter(property = "db.verify.unsupportedFile", required = true)
    protected String unsupportedFile;

    /** 为 true 时跳过校验逻辑 */
    @Parameter(property = "db.verify.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * 基于 Maven 运行时 classpath 构建 {@link URLClassLoader}，供解析 Liquibase 变更集等类资源。
     */
    ClassLoader classLoader() throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> elements = project.getRuntimeClasspathElements();
        URL[] urls = new URL[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            urls[i] = new File(elements.get(i)).toURI().toURL();
        }
        return new URLClassLoader(urls, null);
    }
}
