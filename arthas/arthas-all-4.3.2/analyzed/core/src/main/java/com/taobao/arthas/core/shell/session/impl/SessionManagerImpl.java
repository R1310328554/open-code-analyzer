package com.taobao.arthas.core.shell.session.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.command.model.MessageModel;
import com.taobao.arthas.core.distribution.ResultConsumer;
import com.taobao.arthas.core.distribution.SharingResultDistributor;
import com.taobao.arthas.core.shell.ShellServerOptions;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.session.SessionManager;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.system.JobController;
import com.taobao.arthas.core.shell.system.impl.InternalCommandManager;

import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.concurrent.*;

/**
 * {@link SessionManager} 实现：ConcurrentHashMap 管理 Session，定时回收空闲会话与消费者。
 * <p>
 * 用于 Web Console 等场景；关闭时会通知各 Session 的分发器并中断前台 Job。
 *
 * @author gongdewei 2020-03-20
 */
public class SessionManagerImpl implements SessionManager {
    private static final Logger logger = LoggerFactory.getLogger(SessionManagerImpl.class);
    private final InternalCommandManager commandManager;
    private final Instrumentation instrumentation;
    private final JobController jobController;
    /** 会话空闲超时阈值（毫秒） */
    private final long sessionTimeoutMillis;
    /** 结果消费者无活跃超时（毫秒），默认 5 分钟 */
    private final int consumerTimeoutMillis;
    private final long reaperInterval;
    /** sessionId → Session 映射表 */
    private final Map<String, Session> sessions;
    private final long pid;
    private boolean closed = false;
    private ScheduledExecutorService scheduledExecutorService;

    public SessionManagerImpl(ShellServerOptions options, InternalCommandManager commandManager,
                              JobController jobController) {
        this.commandManager = commandManager;
        this.jobController = jobController;
        this.sessions = new ConcurrentHashMap<String, Session>();
        this.sessionTimeoutMillis = options.getSessionTimeout();
        this.consumerTimeoutMillis = 5 * 60 * 1000; // 消费者 5 分钟无轮询则剔除
        this.reaperInterval = options.getReaperInterval();
        this.instrumentation = options.getInstrumentation();
        this.pid = options.getPid();
        // 启动定时任务扫描并回收超时 Session
        this.setEvictTimer();
    }


    @Override
    public Session createSession() {
        Session session = new SessionImpl();
        session.put(Session.COMMAND_MANAGER, commandManager);
        session.put(Session.INSTRUMENTATION, instrumentation);
        session.put(Session.PID, pid);
        // Web 会话不绑定 ShellServer/Term（与 TTY Shell 路径不同）
        //session.put(Session.TTY, term);
        String sessionId = UUID.randomUUID().toString();
        session.put(Session.ID, sessionId);

        sessions.put(sessionId, session);
        return session;
    }

    @Override
    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public Session removeSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }

        // 移除会话前先中断其前台 Job
        Job job = session.getForegroundJob();
        if (job != null) {
            job.interrupt();
        }

        SharingResultDistributor resultDistributor = session.getResultDistributor();
        if (resultDistributor != null) {
            resultDistributor.close();
        }

        return sessions.remove(sessionId);
    }

    @Override
    public void updateAccessTime(Session session) {
        session.setLastAccessTime(System.currentTimeMillis());
    }

    @Override
    public void close() {
        // TODO：Arthas 关闭时进一步清理资源
        closed = true;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }

        ArrayList<Session> sessions = new ArrayList<Session>(this.sessions.values());
        for (Session session : sessions) {
            SharingResultDistributor resultDistributor = session.getResultDistributor();
            if (resultDistributor != null) {
                resultDistributor.appendResult(new MessageModel("arthas server is going to shutdown."));
            }
            logger.info("Removing session before shutdown: {}, last access time: {}", session.getSessionId(), session.getLastAccessTime());
            this.removeSession(session.getSessionId());
        }

        jobController.close();
    }

    /** 启动单线程定时器周期性调用 {@link #evictSessions} */
    private synchronized void setEvictTimer() {
        if (!closed && reaperInterval > 0) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    final Thread t = new Thread(r, "arthas-session-manager");
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

    /** 扫描超时且无前台 Job 的会话，通知分发器后移除 */
    public void evictSessions() {
        long now = System.currentTimeMillis();
        List<Session> toClose = new ArrayList<Session>();
        for (Session session : sessions.values()) {
            // 仍有 Job 时不回收（如 trace 长时间阻塞）
            // e.g. trace command might wait for a long time before condition is met
            // TODO：还应检查后台 Job 数量
            if (now - session.getLastAccessTime() > sessionTimeoutMillis && session.getForegroundJob() == null) {
                toClose.add(session);
            }
            evictConsumers(session);
        }
        for (Session session : toClose) {
            //interrupt foreground job
            Job job = session.getForegroundJob();
            if (job != null) {
                job.interrupt();
            }
            long timeOutInMinutes = sessionTimeoutMillis / 1000 / 60;
            String reason = "session is inactive for " + timeOutInMinutes + " min(s).";
            SharingResultDistributor resultDistributor = session.getResultDistributor();
            if (resultDistributor != null) {
                resultDistributor.appendResult(new MessageModel(reason));
            }
            this.removeSession(session.getSessionId());
            logger.info("Removing inactive session: {}, last access time: {}", session.getSessionId(), session.getLastAccessTime());
        }
    }

    /** 剔除长时间未轮询的结果消费者，避免 Web 页面泄漏 */
    public void evictConsumers(Session session) {
        SharingResultDistributor distributor = session.getResultDistributor();
        if (distributor != null) {
            List<ResultConsumer> consumers = distributor.getConsumers();
            // 直接从分发器移除不活跃消费者
            long now = System.currentTimeMillis();
            for (ResultConsumer consumer : consumers) {
                long inactiveTime = now - consumer.getLastAccessTime();
                if (inactiveTime > consumerTimeoutMillis) {
                    // 不活跃时长须大于 poll 间隔上限才剔除
                    logger.info("Removing inactive consumer from session, sessionId: {}, consumerId: {}, inactive duration: {}",
                            session.getSessionId(), consumer.getConsumerId(), inactiveTime);
                    consumer.appendResult(new MessageModel("consumer is inactive for a while, please refresh the page."));
                    distributor.removeConsumer(consumer);
                }
            }
        }
    }

    @Override
    public InternalCommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    @Override
    public JobController getJobController() {
        return jobController;
    }
}
