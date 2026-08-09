package com.taobao.arthas.core.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taobao.arthas.core.mcp.tool.util.McpToolUtils;
import com.taobao.arthas.mcp.server.CommandExecutor;
import com.taobao.arthas.mcp.server.protocol.config.McpServerProperties;
import com.taobao.arthas.mcp.server.protocol.config.McpServerProperties.ServerProtocol;
import com.taobao.arthas.mcp.server.protocol.server.McpNettyServer;
import com.taobao.arthas.mcp.server.protocol.server.McpServer;
import com.taobao.arthas.mcp.server.protocol.server.McpStatelessNettyServer;
import com.taobao.arthas.mcp.server.protocol.server.handler.McpHttpRequestHandler;
import com.taobao.arthas.mcp.server.protocol.server.handler.McpStatelessHttpRequestHandler;
import com.taobao.arthas.mcp.server.protocol.server.handler.McpStreamableHttpRequestHandler;
import com.taobao.arthas.mcp.server.protocol.server.transport.NettyStatelessServerTransport;
import com.taobao.arthas.mcp.server.protocol.server.transport.NettyStreamableServerTransportProvider;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema.Implementation;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema.ServerCapabilities;
import com.taobao.arthas.mcp.server.protocol.spec.McpStreamableServerTransportProvider;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.session.ArthasCommandSessionManager;
import com.taobao.arthas.mcp.server.task.InMemoryTaskMessageQueue;
import com.taobao.arthas.mcp.server.task.InMemoryTaskStore;
import com.taobao.arthas.mcp.server.task.TaskAwareToolSpecification;
import com.taobao.arthas.mcp.server.task.TaskMessageQueue;
import com.taobao.arthas.mcp.server.task.TaskStore;
import com.taobao.arthas.mcp.server.tool.DefaultToolCallbackProvider;
import com.taobao.arthas.mcp.server.tool.ToolCallback;
import com.taobao.arthas.mcp.server.tool.ToolCallbackCreateTaskHandler;
import com.taobao.arthas.mcp.server.tool.ToolCallbackProvider;
import com.taobao.arthas.mcp.server.tool.definition.ToolDefinition;
import com.taobao.arthas.mcp.server.util.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Arthas MCP 服务器：在 Arthas 启动后通过 HTTP 暴露 MCP 协议服务。
 * <p>
 * 支持 STREAMABLE（流式会话 + 任务）与 STATELESS（无状态）两种传输模式；
 * 自动扫描 {@link #ARTHAS_TOOL_BASE_PACKAGE} 下的工具并按 taskSupport 分类注册。
 */
public class ArthasMcpServer {
    private static final Logger logger = LoggerFactory.getLogger(ArthasMcpServer.class);

    /** core 模块中 Arthas MCP 工具类的扫描根包 */
    public static final String ARTHAS_TOOL_BASE_PACKAGE = "com.taobao.arthas.core.mcp.tool.function";
    /** MCP 组件优雅关闭的最大等待秒数 */
    private static final long MCP_COMPONENT_STOP_TIMEOUT_SECONDS = 5L;
    /** 任务线程池关闭的最大等待秒数 */
    private static final long MCP_TASK_EXECUTOR_STOP_TIMEOUT_SECONDS = 5L;

    /** STREAMABLE 模式 Netty 服务器 */
    private McpNettyServer streamableServer;
    /** STATELESS 模式 Netty 服务器 */
    private McpStatelessNettyServer statelessServer;

    /** MCP HTTP 端点路径 */
    private final String mcpEndpoint;
    /** 当前启用的传输协议 */
    private final ServerProtocol protocol;

    /** Arthas 命令执行器 */
    private final CommandExecutor commandExecutor;
    /** STREAMABLE 模式下的命令会话管理器 */
    private ArthasCommandSessionManager sessionManager;

    /** 统一 MCP HTTP 请求分发器 */
    private McpHttpRequestHandler unifiedMcpHandler;

    /** STREAMABLE 模式专用处理器 */
    private McpStreamableHttpRequestHandler streamableHandler;

    /** STATELESS 模式专用处理器 */
    private McpStatelessHttpRequestHandler statelessHandler;

    // MCP Task 专用线程池，大小与 maxConcurrentTaskSessions 对齐，
    // 避免 I/O 密集型任务污染 ForkJoinPool.commonPool
    private ExecutorService taskExecutor;

    /** 默认 MCP HTTP 端点 */
    public static final String DEFAULT_MCP_ENDPOINT = "/mcp";
    
    /**
     * 构造 MCP 服务器（尚未启动，需调用 {@link #start()}）。
     *
     * @param mcpEndpoint     端点路径，{@code null} 时使用 {@link #DEFAULT_MCP_ENDPOINT}
     * @param commandExecutor 命令执行器
     * @param protocol        协议名，无效值回退为 STREAMABLE
     */
    public ArthasMcpServer(String mcpEndpoint, CommandExecutor commandExecutor, String protocol) {
        this.mcpEndpoint = mcpEndpoint != null ? mcpEndpoint : DEFAULT_MCP_ENDPOINT;
        this.commandExecutor = commandExecutor;
        
        ServerProtocol resolvedProtocol = ServerProtocol.STREAMABLE;
        if (protocol != null && !protocol.trim().isEmpty()) {
            try {
                resolvedProtocol = ServerProtocol.valueOf(protocol.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid MCP protocol: {}. Using default: STREAMABLE", protocol);
            }
        }
        this.protocol = resolvedProtocol;
    }

    /** 返回统一 MCP HTTP 请求处理器（两种模式共用入口） */
    public McpHttpRequestHandler getMcpRequestHandler() {
        return unifiedMcpHandler;
    }

    /**
     * 启动 MCP 服务器
     */
    public void start() {
        try {
            // 注册 Arthas 特定的 JSON 过滤器
            com.taobao.arthas.core.mcp.util.McpObjectVOFilter.register();

            McpServerProperties properties = new McpServerProperties.Builder()
                    .name("arthas-mcp-server")
                    .version("4.3.2")
                    .mcpEndpoint(mcpEndpoint)
                    .toolChangeNotification(true)
                    .resourceChangeNotification(true)
                    .promptChangeNotification(true)
                    .objectMapper(JsonParser.getObjectMapper())
                    .protocol(this.protocol)
                    .build();

            ToolClassification toolClassification = scanAndClassifyTools();

            unifiedMcpHandler = McpHttpRequestHandler.builder()
                    .mcpEndpoint(properties.getMcpEndpoint())
                    .objectMapper(properties.getObjectMapper())
                    .protocol(properties.getProtocol())
                    .build();

            if (properties.getProtocol() == ServerProtocol.STREAMABLE) {
                startStreamableServer(properties, toolClassification);
            } else {
                startStatelessServer(properties, toolClassification);
            }

            logger.info("Arthas MCP server started successfully");
            logger.info("- MCP Endpoint: {}", properties.getMcpEndpoint());
            logger.info("- Transport mode: {}", properties.getProtocol());
        } catch (Exception e) {
            logger.error("Failed to start Arthas MCP server", e);
            throw new RuntimeException("Failed to start Arthas MCP server", e);
        }
    }

    /**
     * 扫描并分类工具
     */
    private ToolClassification scanAndClassifyTools() {
        DefaultToolCallbackProvider toolCallbackProvider = new DefaultToolCallbackProvider();
        toolCallbackProvider.setToolBasePackage(ARTHAS_TOOL_BASE_PACKAGE);
        
        ToolCallback[] allCallbacks = toolCallbackProvider.getToolCallbacks();
        
        // 根据 taskSupport 属性分类工具
        List<ToolCallback> requiredTaskTools = new ArrayList<>();  // taskSupport=required
        List<ToolCallback> optionalTaskTools = new ArrayList<>();  // taskSupport=optional
        List<ToolCallback> normalTools = new ArrayList<>();        // taskSupport=forbidden
        
        for (ToolCallback callback : allCallbacks) {
            if (callback == null) {
                continue;
            }
            
            ToolDefinition def = callback.getToolDefinition();
            McpSchema.TaskSupportMode taskSupport = def.taskSupport();
            
            // 根据 taskSupport 分类
            switch (taskSupport) {
                case REQUIRED:
                    requiredTaskTools.add(callback);
                    break;
                case OPTIONAL:
                    optionalTaskTools.add(callback);
                    break;
                case FORBIDDEN:
                default:
                    normalTools.add(callback);
                    break;
            }
        }
        
        logger.info("Scanned {} tools: {} normal, {} optional-task, {} required-task", 
                allCallbacks.length, normalTools.size(), optionalTaskTools.size(), requiredTaskTools.size());
        
        return new ToolClassification(Arrays.asList(allCallbacks), normalTools, optionalTaskTools, requiredTaskTools);
    }
    
    /**
     * 启动 Streamable 模式服务器
     */
    private void startStreamableServer(McpServerProperties properties, ToolClassification classification) {
        // 初始化 SessionManager
        this.sessionManager = new ArthasCommandSessionManager(commandExecutor);
        logger.info("Initialized ArthasCommandSessionManager for MCP server");

        int maxSessions = com.taobao.arthas.mcp.server.task.TaskDefaults.DEFAULT_MAX_CONCURRENT_TASK_SESSIONS;
        this.taskExecutor = new ThreadPoolExecutor(
                maxSessions,                       // corePoolSize: 与 session 上限一致
                maxSessions,                       // maxPoolSize: 固定大小，避免动态扩缩
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),           // 无缓冲：直接交付或拒绝，无隐式排队
                new McpTaskThreadFactory(),         // 具名线程，便于 thread dump 诊断
                new ThreadPoolExecutor.AbortPolicy() // 兜底：超出直接抛 RejectedExecutionException
        );
        logger.info("Created MCP task executor: fixedSize={}, queue=SynchronousQueue (no buffering)",
                maxSessions);
        
        McpStreamableServerTransportProvider transportProvider = createStreamableHttpTransportProvider(properties);
        streamableHandler = transportProvider.getMcpRequestHandler();
        unifiedMcpHandler.setStreamableHandler(streamableHandler);

        // 准备任务感知工具列表（taskSupport = OPTIONAL 或 REQUIRED）
        List<ToolCallback> taskAwareTools = new ArrayList<>();
        taskAwareTools.addAll(classification.optionalTaskTools);
        taskAwareTools.addAll(classification.requiredTaskTools);
        
        boolean hasTaskTools = !taskAwareTools.isEmpty();

        McpServer.StreamableServerNettySpecification serverSpec = McpServer.netty(transportProvider)
                .serverInfo(new Implementation(properties.getName(), properties.getVersion()))
                .capabilities(buildServerCapabilities(properties, hasTaskTools))
                .instructions(properties.getInstructions())
                .requestTimeout(properties.getRequestTimeout())
                .commandExecutor(commandExecutor)
                .sessionManager(this.sessionManager)
                .objectMapper(properties.getObjectMapper() != null ? properties.getObjectMapper() : JsonParser.getObjectMapper());

        // 只注册普通工具（taskSupport = FORBIDDEN）
        serverSpec.tools(McpToolUtils.toStreamableToolSpecifications(classification.normalTools));
        logger.debug("Registered {} normal tools", classification.normalTools.size());
        
        if (hasTaskTools) {
            configureTaskSupport(serverSpec, taskAwareTools);
        }

        streamableServer = serverSpec.build();
    }

    /**
     * 配置任务支持
     */
    private void configureTaskSupport(McpServer.StreamableServerNettySpecification serverSpec,
                                      List<ToolCallback> taskAwareTools) {
        logger.info("Configuring tasks support for {} task-aware tools", taskAwareTools.size());

        // 创建 TaskStore 和 TaskMessageQueue
        TaskStore<McpSchema.ServerTaskPayloadResult> taskStore = InMemoryTaskStore.<McpSchema.ServerTaskPayloadResult>builder()
                .defaultTtl(Duration.ofMinutes(30))  // 任务 TTL 30 分钟
                .build();

        TaskMessageQueue messageQueue = new InMemoryTaskMessageQueue();

        // 配置 TaskStore 和 TaskMessageQueue
        serverSpec.taskStore(taskStore).taskMessageQueue(messageQueue);

        // 为每个任务感知工具创建 TaskAwareToolSpecification
        for (ToolCallback callback : taskAwareTools) {
            ToolDefinition def = callback.getToolDefinition();

            ToolCallbackCreateTaskHandler createTaskHandler = new ToolCallbackCreateTaskHandler(callback, taskExecutor);

            TaskAwareToolSpecification spec = TaskAwareToolSpecification.builder()
                    .name(def.getName())
                    .description(def.getDescription())
                    .inputSchema(def.getInputSchema())
                    .taskSupport(def.taskSupport())
                    .createTaskHandler(createTaskHandler)
                    .build();

            serverSpec.taskTool(spec);
            logger.debug("Registered task-aware tool: {} (taskSupport: {})", def.getName(), def.taskSupport());
        }

        logger.info("Registered {} task-aware tools successfully", taskAwareTools.size());
    }
    
    /**
     * 启动 Stateless 模式服务器
     */
    private void startStatelessServer(McpServerProperties properties, ToolClassification classification) {
        // 创建传输层
        NettyStatelessServerTransport statelessTransport = createStatelessHttpTransport(properties);
        statelessHandler = statelessTransport.getMcpRequestHandler();
        unifiedMcpHandler.setStatelessHandler(statelessHandler);
        
        // Stateless 模式不支持任务
        boolean enableTasks = false;
        
        // 构建服务器规格
        McpServer.StatelessServerNettySpecification serverSpec = McpServer.netty(statelessTransport)
                .serverInfo(new Implementation(properties.getName(), properties.getVersion()))
                .capabilities(buildServerCapabilities(properties, enableTasks))
                .instructions(properties.getInstructions())
                .requestTimeout(properties.getRequestTimeout())
                .commandExecutor(commandExecutor)
                .objectMapper(properties.getObjectMapper() != null ? properties.getObjectMapper() : JsonParser.getObjectMapper());
        
        // 在 stateless 模式下，所有工具都作为普通工具注册（不支持任务）
        serverSpec.tools(McpToolUtils.toStatelessToolSpecifications(classification.allCallbacks));
        logger.info("Registered {} tools in stateless mode (tasks not supported)", classification.allCallbacks.size());
        
        // 构建并启动服务器
        statelessServer = serverSpec.build();
    }
    
    /**
     * 工具分类结果
     */
    private static class ToolClassification {
        final List<ToolCallback> allCallbacks;
        final List<ToolCallback> normalTools;
        final List<ToolCallback> optionalTaskTools;
        final List<ToolCallback> requiredTaskTools;
        
        ToolClassification(List<ToolCallback> allCallbacks, 
                          List<ToolCallback> normalTools,
                          List<ToolCallback> optionalTaskTools,
                          List<ToolCallback> requiredTaskTools) {
            this.allCallbacks = allCallbacks;
            this.normalTools = normalTools;
            this.optionalTaskTools = optionalTaskTools;
            this.requiredTaskTools = requiredTaskTools;
        }
    }
    
    /** MCP 服务器默认 keep-alive 间隔（15 秒） */
    public static final Duration DEFAULT_KEEP_ALIVE_INTERVAL = Duration.ofSeconds(15);
    
    /** 创建 STREAMABLE 模式的 Netty HTTP 传输提供者 */
    private NettyStreamableServerTransportProvider createStreamableHttpTransportProvider(McpServerProperties properties) {
        return NettyStreamableServerTransportProvider.builder()
                .mcpEndpoint(properties.getMcpEndpoint())
                .objectMapper(properties.getObjectMapper() != null ? properties.getObjectMapper() : new ObjectMapper())
                .keepAliveInterval(DEFAULT_KEEP_ALIVE_INTERVAL)
                .build();
    }

    /** 创建 STATELESS 模式的 Netty HTTP 传输层 */
    private NettyStatelessServerTransport createStatelessHttpTransport(McpServerProperties properties) {
        return NettyStatelessServerTransport.builder()
                .mcpEndpoint(properties.getMcpEndpoint())
                .objectMapper(properties.getObjectMapper() != null ? properties.getObjectMapper() : new ObjectMapper())
                .build();
    }

    /**
     * 构建服务器能力声明。
     * 
     * @param properties 服务器属性
     * @param enableTasks 是否启用任务支持（只有在有任务工具时才启用）
     * @return ServerCapabilities
     */
    private ServerCapabilities buildServerCapabilities(McpServerProperties properties, boolean enableTasks) {
        ServerCapabilities.Builder builder = ServerCapabilities.builder()
                .prompts(new ServerCapabilities.PromptCapabilities(properties.isPromptChangeNotification()))
                .resources(new ServerCapabilities.ResourceCapabilities(properties.isResourceSubscribe(), properties.isResourceChangeNotification()))
                .tools(new ServerCapabilities.ToolCapabilities(properties.isToolChangeNotification()));
        
        // 只有在有任务工具时才声明 tasks capability
        if (enableTasks) {
            // 声明服务器支持的任务能力
            ServerCapabilities.TaskCapabilities taskCapabilities = ServerCapabilities.TaskCapabilities.builder()
                    .list()        // 支持 tasks/list（列出所有任务）
                    .cancel()      // 支持 tasks/cancel（取消任务）
                    .toolsCall()   // 支持 tools/call 的任务增强执行（包括 tasks/get 和 tasks/result）
                    .build();
            
            builder.tasks(taskCapabilities);
            logger.info("Tasks capability enabled (supports list, cancel, tools/call with tasks)");
        } else {
            logger.info("Tasks capability disabled (no task-aware tools)");
        }
        
        return builder.build();
    }

    /** 依次关闭统一处理器、流式/无状态服务器及任务线程池 */
    public void stop() {
        logger.info("Stopping Arthas MCP server...");
        if (unifiedMcpHandler != null) {
            logger.debug("Shutting down unified MCP handler");
            closeMcpComponent("Unified MCP handler", () -> unifiedMcpHandler.closeGracefully());
        }

        if (streamableServer != null) {
            logger.debug("Shutting down streamable server");
            closeMcpComponent("Streamable server", () -> streamableServer.closeGracefully());
        }

        if (statelessServer != null) {
            logger.debug("Shutting down stateless server");
            closeMcpComponent("Stateless server", () -> statelessServer.closeGracefully());
        }

        stopTaskExecutor();
        logger.info("Arthas MCP server stopped completely");
    }

    /** 带超时的 MCP 组件优雅关闭，失败时记录警告并继续整体停机流程 */
    private void closeMcpComponent(String componentName, Supplier<CompletableFuture<Void>> closeAction) {
        try {
            CompletableFuture<Void> closeFuture = closeAction.get();
            if (closeFuture == null) {
                logger.warn("{} graceful shutdown returned null future, continue stopping Arthas MCP server", componentName);
                return;
            }

            closeFuture.get(MCP_COMPONENT_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            logger.info("{} stopped successfully", componentName);
        } catch (TimeoutException e) {
            logger.warn("{} graceful shutdown timed out after {}s, continue stopping Arthas MCP server",
                    componentName, MCP_COMPONENT_STOP_TIMEOUT_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("{} graceful shutdown interrupted, continue stopping Arthas MCP server", componentName, e);
        } catch (Exception e) {
            logger.warn("{} graceful shutdown failed, continue stopping Arthas MCP server", componentName, e);
        }
    }

    /** 关闭 MCP 任务专用线程池，超时则 {@link ThreadPoolExecutor#shutdownNow()} */
    private void stopTaskExecutor() {
        if (taskExecutor == null) {
            return;
        }

        try {
            taskExecutor.shutdown();
            if (!taskExecutor.awaitTermination(MCP_TASK_EXECUTOR_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
                logger.warn("MCP task executor did not terminate within {}s, forced shutdown",
                        MCP_TASK_EXECUTOR_STOP_TIMEOUT_SECONDS);
            } else {
                logger.info("MCP task executor stopped successfully");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            taskExecutor.shutdownNow();
            logger.warn("MCP task executor shutdown interrupted, forced shutdown", e);
        } catch (Exception e) {
            try {
                if (!taskExecutor.isShutdown()) {
                    taskExecutor.shutdownNow();
                }
            } catch (Exception shutdownException) {
                logger.warn("Failed to force shutdown MCP task executor", shutdownException);
            }
            logger.warn("Failed to stop MCP task executor", e);
        }
    }


    /** 为 MCP 异步任务创建具名守护线程（{@code mcp-task-N}） */
    private static class McpTaskThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "mcp-task-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
