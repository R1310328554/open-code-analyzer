package com.taobao.arthas.core.mcp.tool.function.klass100;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * ClassLoader MCP Tool：查看与管理 JVM 中的类加载器。
 * <p>
 * 对应 {@code classloader} 命令；可输出统计、实例列表、继承树、URL 与类映射等。
 * 搜索已加载类请优先使用 {@code sc}（SearchClassTool）。
 */
public class ClassLoaderTool extends AbstractArthasTool {

    /** 默认模式：输出各 ClassLoader 加载类数量等统计 */
    public static final String MODE_STATS = "stats";
    /** 列出 ClassLoader 实例详情（-l） */
    public static final String MODE_INSTANCES = "instances";
    /** 打印 ClassLoader 继承树（-t） */
    public static final String MODE_TREE = "tree";
    /** 列出某 ClassLoader 已加载的全部类（-a，慎用） */
    public static final String MODE_ALL_CLASSES = "all-classes";
    /** URL/jar 资源统计（--url-stat） */
    public static final String MODE_URL_STATS = "url-stats";
    /** URL 与类名对应关系（--url-classes） */
    public static final String MODE_URL_CLASSES = "url-classes";

    /**
     * classloader 诊断工具
     * 支持多种显示模式、指定 ClassLoader、资源查找与显式 load 等。
     */
    @Tool(
            name = "classloader",
            description = "ClassLoader 诊断工具，可以查看类加载器统计信息、继承树、URLs，以及进行资源查找和类加载操作。搜索类的场景优先使用 sc 工具"
    )
    public String classloader(
            @ToolParam(description = "显示模式：stats(统计信息，默认), instances(实例详情), tree(继承树), all-classes(所有类，慎用), url-stats(URL统计), url-classes(URL与类关系)", required = false)
            String mode,

            @ToolParam(description = "ClassLoader的hashcode（16进制），用于指定特定的ClassLoader", required = false)
            String classLoaderHash,

            @ToolParam(description = "ClassLoader的完整类名，如sun.misc.Launcher$AppClassLoader，可替代hashcode", required = false)
            String classLoaderClass,

            @ToolParam(description = "要查找的资源名称，如META-INF/MANIFEST.MF", required = false)
            String resource,

            @ToolParam(description = "要加载的类名，支持全限定名", required = false)
            String loadClass,

            @ToolParam(description = "详情模式：列出每个 URL/jar 中的类名（等价于 -d），仅在 mode=url-classes 时生效", required = false)
            Boolean details,

            @ToolParam(description = "按 jar 包名/URL 关键字过滤，仅在 mode=url-classes 时生效", required = false)
            String jar,

            @ToolParam(description = "按类名/包名关键字过滤，仅在 mode=url-classes 时生效", required = false)
            String classFilter,

            @ToolParam(description = "是否使用正则匹配 jar/class（等价于 -E），仅在 mode=url-classes 时生效", required = false)
            Boolean regex,

            @ToolParam(description = "详情模式下每个 URL/jar 最多展示类数量（等价于 -n），默认 100，仅在 mode=url-classes 时生效", required = false)
            Integer limit,

            ToolContext toolContext) {
        StringBuilder cmd = buildCommand("classloader");

        // 按 mode 映射到 classloader 子命令开关
        if (mode != null) {
            switch (mode.toLowerCase()) {
                case MODE_INSTANCES:
                    cmd.append(" -l");
                    break;
                case MODE_TREE:
                    cmd.append(" -t");
                    break;
                case MODE_ALL_CLASSES:
                    cmd.append(" -a");
                    break;
                case MODE_URL_STATS:
                    cmd.append(" --url-stat");
                    break;
                case MODE_URL_CLASSES:
                    cmd.append(" --url-classes");
                    break;
                case MODE_STATS:
                default:
                    break;
            }
        }

        // 限定目标 ClassLoader（hash 优先）
        if (classLoaderHash != null && !classLoaderHash.trim().isEmpty()) {
            addParameter(cmd, "-c", classLoaderHash);
        } else if (classLoaderClass != null && !classLoaderClass.trim().isEmpty()) {
            addParameter(cmd, "--classLoaderClass", classLoaderClass);
        }

        addParameter(cmd, "-r", resource);

        addParameter(cmd, "--load", loadClass);

        // url-classes 模式下的过滤与详情参数
        if (mode != null && MODE_URL_CLASSES.equalsIgnoreCase(mode)) {
            addFlag(cmd, "-d", details);
            addFlag(cmd, "-E", regex);
            if (limit != null && limit > 0) {
                addParameter(cmd, "-n", String.valueOf(limit));
            }
            addParameter(cmd, "--jar", jar);
            addParameter(cmd, "--class", classFilter);
        }

        return executeSync(toolContext, cmd.toString());
    }
}
