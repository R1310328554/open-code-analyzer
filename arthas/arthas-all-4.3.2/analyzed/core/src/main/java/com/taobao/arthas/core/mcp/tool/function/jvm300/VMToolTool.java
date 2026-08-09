package com.taobao.arthas.core.mcp.tool.function.jvm300;

import com.taobao.arthas.core.mcp.tool.function.AbstractArthasTool;
import com.taobao.arthas.mcp.server.tool.ToolContext;
import com.taobao.arthas.mcp.server.tool.annotation.Tool;
import com.taobao.arthas.mcp.server.tool.annotation.ToolParam;

/**
 * VMTool MCP Tool：通过 JVMTI 等能力在目标 JVM 内执行高级诊断操作。
 * <p>
 * 对应 {@code vmtool} 命令；支持按类查询堆内实例、引用链分析、强制 GC、
 * 中断指定线程等。不同 {@code action} 所需参数不同，调用前需校验必填项。
 */
public class VMToolTool extends AbstractArthasTool {

    /** 按类名获取堆内实例列表 */
    public static final String ACTION_GET_INSTANCES = "getInstances";
    /** 分析指定类实例的引用关系（谁在引用它） */
    public static final String ACTION_REFERENCE_ANALYZE = "referenceAnalyze";
    /** 向指定线程发送中断信号 */
    public static final String ACTION_INTERRUPT_THREAD = "interruptThread";

    /**
     * vmtool 虚拟机工具诊断
     * 支持:
     * - action: 操作类型（getInstances/forceGc/interruptThread 等）
     * - classLoaderHash / classLoaderClass: 限定 ClassLoader 上下文
     * - className: getInstances/referenceAnalyze 时必填
     * - limit / expandLevel / express: getInstances 专用
     * - threadId: interruptThread 时必填
     */
    @Tool(
            name = "vmtool",
            description = "虚拟机工具诊断工具: 查询实例、强制 GC、线程中断等，对应 Arthas 的 vmtool 命令。"
    )
    public String vmtool(
            @ToolParam(description = "操作类型: getInstances/forceGc/interruptThread 等")
            String action,

            @ToolParam(description = "ClassLoader的hashcode（16进制），用于指定特定的ClassLoader", required = false)
            String classLoaderHash,

            @ToolParam(description = "ClassLoader的完整类名，如sun.misc.Launcher$AppClassLoader，可替代hashcode", required = false)
            String classLoaderClass,

            @ToolParam(description = "类名，全限定（getInstances/referenceAnalyze 时使用）", required = false)
            String className,

            @ToolParam(description = "返回实例限制数量 (-l)，getInstances 时使用，默认 10；<=0 表示不限制", required = false)
            Integer limit,

            @ToolParam(description = "结果对象展开层次 (-x)，默认 1", required = false)
            Integer expandLevel,

            @ToolParam(description = "OGNL 表达式，对 getInstances 返回的 instances 执行 (--express)", required = false)
            String express,

            @ToolParam(description = "线程 ID (-t)，interruptThread 时使用", required = false)
            Long threadId,

            ToolContext toolContext
    ) {
        StringBuilder cmd = buildCommand("vmtool");

        // action 为必填，决定后续子命令分支
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("vmtool: action 参数不能为空");
        }
        String normalizedAction = action.trim();
        cmd.append(" --action ").append(normalizedAction);

        // ClassLoader 定位：hash 优先于类名
        if (classLoaderHash != null && !classLoaderHash.trim().isEmpty()) {
            addParameter(cmd, "-c", classLoaderHash);
        } else if (classLoaderClass != null && !classLoaderClass.trim().isEmpty()) {
            addParameter(cmd, "--classLoaderClass", classLoaderClass);
        }

        // 实例查询与引用分析均需要目标类名
        if (ACTION_GET_INSTANCES.equals(normalizedAction) || ACTION_REFERENCE_ANALYZE.equals(normalizedAction)) {
            if (className == null || className.trim().isEmpty()) {
                throw new IllegalArgumentException("vmtool " + normalizedAction + " 需要指定类名 (className)");
            }
            addParameter(cmd, "--className", className);
        }

        // getInstances 专属：数量限制、展开深度与 OGNL 后处理
        if (ACTION_GET_INSTANCES.equals(normalizedAction)) {
            if (limit != null) {
                cmd.append(" --limit ").append(limit);
            }
            if (expandLevel != null && expandLevel > 0) {
                cmd.append(" -x ").append(expandLevel);
            }
            if (express != null && !express.trim().isEmpty()) {
                addParameter(cmd, "--express", express);
            }
        }

        // interruptThread 必须提供有效线程 ID
        if (ACTION_INTERRUPT_THREAD.equals(normalizedAction)) {
            if (threadId != null && threadId > 0) {
                cmd.append(" -t ").append(threadId);
            } else {
                throw new IllegalArgumentException("vmtool interruptThread 需要指定线程 ID (threadId)");
            }
        }

        return executeSync(toolContext, cmd.toString());
    }


}
