package com.taobao.arthas.core.shell.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.alibaba.arthas.tunnel.client.TunnelClient;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.Shell;
import com.taobao.arthas.core.shell.ShellServer;
import com.taobao.arthas.core.shell.ShellServerOptions;
import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.handlers.server.SessionClosedHandler;
import com.taobao.arthas.core.shell.handlers.server.SessionsClosedHandler;
import com.taobao.arthas.core.shell.handlers.server.TermServerListenHandler;
import com.taobao.arthas.core.shell.handlers.server.TermServerTermHandler;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.system.impl.GlobalJobControllerImpl;
import com.taobao.arthas.core.shell.system.impl.InternalCommandManager;
import com.taobao.arthas.core.shell.system.impl.JobControllerImpl;
import com.taobao.arthas.core.shell.term.Term;
import com.taobao.arthas.core.shell.term.TermServer;
import com.taobao.arthas.core.util.ArthasBanner;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ShellServer} 默认实现：管理 TermServer、会话池与全局 Job 控制器。
 * <p>
 * 监听多个终端入口（Telnet/WebSocket），为每条连接创建 {@link ShellImpl}；
 * 定时回收空闲会话，关闭时依次停止 TermServer 并终止所有 Job。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ShellServerImpl extends ShellServer {

    private static final Logger logger = LoggerFactory.getLogger(ShellServerImpl.class);

    /** 已注册的命令解析器列表，支持运行时追加 */
    private final CopyOnWriteArrayList<CommandResolver> resolvers;
    /** 聚合所有 CommandResolver 的内部命令管理器 */
    private final InternalCommandManager commandManager;
    /** 待启动/已注册的终端服务列表（Telnet、HTTP 等） */
    private final List<TermServer> termServers;
    /** 会话空闲超时毫秒数，超时且无 Job 时回收 */
    private final long timeoutMillis;
    /** 会话回收定时器执行间隔（毫秒） */
    private final long reaperInterval;
    private String welcomeMessage;
    private Instrumentation instrumentation;
    private long pid;
    private boolean closed = true;
    /** 活跃 Shell 会话表，键为会话 id */
    private final Map<String, ShellImpl> sessions;
    private final Future<Void> sessionsClosed = Future.future();
    private ScheduledExecutorService scheduledExecutorService;
    /** 全局 Job 控制器，跨会话共享后台任务视图 */
    private JobControllerImpl jobController = new GlobalJobControllerImpl();

    public ShellServerImpl(ShellServerOptions options) {
        this.welcomeMessage = options.getWelcomeMessage();
        this.termServers = new ArrayList<TermServer>();
        this.timeoutMillis = options.getSessionTimeout();
        this.sessions = new ConcurrentHashMap<String, ShellImpl>();
        this.reaperInterval = options.getReaperInterval();
        this.resolvers = new CopyOnWriteArrayList<CommandResolver>();
        this.commandManager = new InternalCommandManager(resolvers);
        this.instrumentation = options.getInstrumentation();
        this.pid = options.getPid();

        // 注册内置命令解析器，使 help 能列出 exit/jobs 等命令
        resolvers.add(new BuiltinCommandResolver());
    }

    @Override
    public synchronized ShellServer registerCommandResolver(CommandResolver resolver) {
        resolvers.add(0, resolver);
        return this;
    }

    @Override
    public synchronized ShellServer registerTermServer(TermServer termServer) {
        termServers.add(termServer);
        return this;
    }

    /** 新终端连接入口：创建 Shell、初始化 readline 并加入会话表 */
    public void handleTerm(Term term) {
        synchronized (this) {
            // 多 TermServer 竞态时服务器可能已关闭，直接拒绝连接
            if (closed) {
                term.close();
                return;
            }
        }

        ShellImpl session = createShell(term);
        tryUpdateWelcomeMessage();
        session.setWelcome(welcomeMessage);
        session.closedFuture.setHandler(new SessionClosedHandler(this, session));
        session.init();
        sessions.put(session.id, session); // init 后再注册，确保 close handler 已绑定
        session.readline(); // 启动交互式命令行
    }

    /** 若已连接 Tunnel，用 agent id 刷新欢迎 Banner */
    private void tryUpdateWelcomeMessage() {
        TunnelClient tunnelClient = ArthasBootstrap.getInstance().getTunnelClient();
        if (tunnelClient != null) {
            String id = tunnelClient.getId();
            if (id != null) {
                Map<String, String> welcomeInfos = new HashMap<String, String>();
                welcomeInfos.put("id", id);
                this.welcomeMessage = ArthasBanner.welcome(welcomeInfos);
            }
        }
    }

    @Override
    public ShellServer listen(final Handler<Future<Void>> listenHandler) {
        final List<TermServer> toStart;
        synchronized (this) {
            if (!closed) {
                throw new IllegalStateException("Server listening");
            }
            toStart = termServers;
        }
        final AtomicInteger count = new AtomicInteger(toStart.size());
        if (count.get() == 0) {
            setClosed(false);
            listenHandler.handle(Future.<Void>succeededFuture());
            return this;
        }
        Handler<Future<TermServer>> handler = new TermServerListenHandler(this, listenHandler, toStart);
        for (TermServer termServer : toStart) {
            termServer.termHandler(new TermServerTermHandler(this));
            termServer.listen(handler);
        }
        return this;
    }

    /** 扫描并关闭超时且无活跃 Job 的空闲会话 */
    private void evictSessions() {
        long now = System.currentTimeMillis();
        Set<ShellImpl> toClose = new HashSet<ShellImpl>();
        for (ShellImpl session : sessions.values()) {
            // 仍有 Job 运行时不关闭（如 trace 长时间等待触发条件）
            // e.g. trace command might wait for a long time before condition is met
            if (now - session.lastAccessedTime() > timeoutMillis && session.jobs().size() == 0) {
                toClose.add(session);
            }
            logger.debug(session.id + ":" + session.lastAccessedTime());
        }
        for (ShellImpl session : toClose) {
            long timeOutInMinutes = timeoutMillis / 1000 / 60;
            String reason = "session is inactive for " + timeOutInMinutes + " min(s).";
            session.close(reason);
        }
    }

    /** 启动守护线程定时执行 {@link #evictSessions} */
    public synchronized void setTimer() {
        if (!closed && reaperInterval > 0) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    final Thread t = new Thread(r, "arthas-shell-server");
                    t.setDaemon(true);
                    return t;
                }
            });
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    evictSessions();
                }
            }, 0, reaperInterval, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void setClosed(boolean closed) {
        this.closed = closed;
    }

    /** 从会话表移除 Shell：先终止前台 Job，再关闭连接并通知全部会话已关闭 */
    public void removeSession(ShellImpl shell) {
        boolean completeSessionClosed;

        Job job = shell.getForegroundJob();
        if (job != null) {
            // 会话断开时终止其前台 Job，避免孤儿进程
            job.terminate();
            logger.info("Session {} closed, so terminate foreground job, id: {}, line: {}",
                        shell.session().getSessionId(), job.id(), job.line());
        }

        synchronized (ShellServerImpl.this) {
            sessions.remove(shell.id);
            shell.close("network error");
            completeSessionClosed = sessions.isEmpty() && closed;
        }
        if (completeSessionClosed) {
            sessionsClosed.complete();
        }
    }

    @Override
    public synchronized Shell createShell() {
        return createShell(null);
    }

    @Override
    /** 创建新的 ShellImpl；服务器已关闭时抛 IllegalStateException */
    public synchronized ShellImpl createShell(Term term) {
        if (closed) {
            throw new IllegalStateException("Closed");
        }
        return new ShellImpl(this, term, commandManager, instrumentation, pid, jobController);
    }

    @Override
    public void close(final Handler<Future<Void>> completionHandler) {
        List<TermServer> toStop;
        List<ShellImpl> toClose;
        synchronized (this) {
            if (closed) {
                toStop = Collections.emptyList();
                toClose = Collections.emptyList();
            } else {
                setClosed(true);
                if (scheduledExecutorService != null) {
                    scheduledExecutorService.shutdownNow();
                }
                toStop = termServers;
                toClose = new ArrayList<ShellImpl>(sessions.values());
                if (toClose.isEmpty()) {
                    sessionsClosed.complete();
                }
            }
        }
        if (toStop.isEmpty() && toClose.isEmpty()) {
            completionHandler.handle(Future.<Void>succeededFuture());
        } else {
            final AtomicInteger count = new AtomicInteger(1 + toClose.size());
            Handler<Future<Void>> handler = new SessionsClosedHandler(count, completionHandler);

            for (ShellImpl shell : toClose) {
                shell.close("server is going to shutdown.");
            }

            for (TermServer termServer : toStop) {
                termServer.close(handler);
            }
            jobController.close();
            sessionsClosed.setHandler(handler);
        }
    }

    /** @return 全局 Job 控制器实例 */
    public JobControllerImpl getJobController() {
        return jobController;
    }

    /** @return 内部命令管理器 */
    public InternalCommandManager getCommandManager() {
        return commandManager;
    }
}
