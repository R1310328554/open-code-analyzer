package com.taobao.arthas.core.mcp;

import com.taobao.arthas.mcp.server.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Arthas MCP 引导类：封装 MCP 服务器的创建、启动与关闭生命周期。
 * <p>
 * 构造时保存 {@link CommandExecutor}、HTTP 端点与传输协议，{@link #start()} 创建
 * {@link ArthasMcpServer} 并暴露 MCP 服务；{@link #getInstance()} 供全局访问单例。
 *
 * @author Yeaury
 */
public class ArthasMcpBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(ArthasMcpBootstrap.class);
    
    /** 已启动的 MCP 服务器实例 */
    private ArthasMcpServer mcpServer;
    /** 执行 Arthas 命令的底层执行器 */
    private final CommandExecutor commandExecutor;
    /** MCP HTTP 端点路径（如 {@code /mcp}） */
    private final String mcpEndpoint;
    /** 传输协议名称（STREAMABLE / STATELESS） */
    private final String protocol;
    /** 全局单例引用，构造时赋值 */
    private static ArthasMcpBootstrap instance;

    /**
     * 创建引导实例并注册为全局单例。
     *
     * @param commandExecutor Arthas 命令执行器
     * @param mcpEndpoint     MCP HTTP 端点
     * @param protocol        传输协议字符串
     */
    public ArthasMcpBootstrap(CommandExecutor commandExecutor, String mcpEndpoint, String protocol) {
        this.commandExecutor = commandExecutor;
        this.mcpEndpoint = mcpEndpoint;
        this.protocol = protocol;
        instance = this;
    }

    /** 返回当前引导单例（可能为 {@code null}，若尚未构造） */
    public static ArthasMcpBootstrap getInstance() {
        return instance;
    }

    /** 返回绑定的命令执行器 */
    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    /**
     * 创建并启动 MCP 服务器。
     *
     * @return 已启动的 {@link ArthasMcpServer} 实例
     * @throws RuntimeException 初始化失败时抛出
     */
    public ArthasMcpServer start() {
        logger.info("Initializing Arthas MCP Bootstrap...");
        try {
            logger.debug("Creating MCP server instance with command executor: {}", 
                    commandExecutor.getClass().getSimpleName());
            
            // 使用 CommandExecutor 与自定义端点创建并启动 MCP 服务器
            mcpServer = new ArthasMcpServer(mcpEndpoint, commandExecutor, protocol);
            logger.debug("MCP server instance created successfully");
            
            mcpServer.start();
            logger.info("Arthas MCP server initialized successfully");
            logger.info("Bootstrap ready - server is operational");
            return mcpServer;
        } catch (Exception e) {
            logger.error("Failed to initialize Arthas MCP server", e);
            throw new RuntimeException("Failed to initialize Arthas MCP server", e);
        }
    }

    /** 优雅关闭 MCP 服务器并释放资源 */
    public void shutdown() {
        logger.info("Initiating Arthas MCP Bootstrap shutdown...");
        if (mcpServer != null) {
            logger.debug("Stopping MCP server...");
            mcpServer.stop();
            logger.info("MCP server stopped");
        } else {
            logger.warn("MCP server was null during shutdown - may not have been properly initialized");
        }
        logger.info("Arthas MCP Bootstrap shutdown completed");
    }
}
