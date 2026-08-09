package com.taobao.arthas.core.shell.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.common.ArthasConstants;
import com.taobao.arthas.core.security.AuthUtils;
import com.taobao.arthas.core.security.SecurityAuthenticator;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.Shell;
import com.taobao.arthas.core.shell.ShellServer;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.cli.CliTokens;
import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.shell.*;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.session.impl.SessionImpl;
import com.taobao.arthas.core.shell.system.ExecStatus;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.system.JobController;
import com.taobao.arthas.core.shell.system.JobListener;
import com.taobao.arthas.core.shell.system.impl.InternalCommandManager;
import com.taobao.arthas.core.shell.system.impl.JobControllerImpl;
import com.taobao.arthas.core.shell.term.Term;
import com.taobao.arthas.core.shell.term.impl.TermImpl;
import com.taobao.arthas.core.shell.term.impl.http.ExtHttpTtyConnection;
import com.taobao.arthas.core.util.Constants;
import com.taobao.arthas.core.util.FileUtils;

import io.netty.channel.ChannelHandlerContext;
import io.termd.core.telnet.TelnetConnection;
import io.termd.core.telnet.TelnetTtyConnection;
import io.termd.core.telnet.netty.NettyTelnetConnection;
import io.termd.core.tty.TtyConnection;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

/**
 * 单个 Shell 会话的实现，代表从 ShellServer 视角看到的一条交互连接。
 * <p>
 * 绑定 {@link Term}、{@link Session} 与 {@link JobController}，负责 readline 循环、
 * 前台 Job 切换、Telnet/HTTP 鉴权信息注入及命令历史持久化。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ShellImpl implements Shell {
    private static final Logger logger = LoggerFactory.getLogger(ShellImpl.class);
    /** Agent 模式终端类型标识，此类连接启用静默会话（不输出欢迎语） */
    private static final String ARTHAS_AGENT_TERMINAL_TYPE = "arthas-agent";

    /** 本会话所属 Job 控制器，管理前台/后台任务 */
    private JobControllerImpl jobController;
    /** 会话唯一标识，写入 Session 供外部引用 */
    final String id;
    /** 会话关闭时完成的 Future，供 ShellServer 聚合等待 */
    final Future<Void> closedFuture;
    private InternalCommandManager commandManager;
    /** 会话上下文，存储 PID、Instrumentation、鉴权 Subject 等 */
    private Session session = new SessionImpl();
    /** 关联的终端（Telnet/WebSocket/HTTP TTY） */
    private Term term;
    /** 连接建立时输出的欢迎信息 */
    private String welcome;
    /** 当前占用终端输入的前台 Job，null 表示空闲可 readline */
    private Job currentForegroundJob;
    /** readline 提示符，形如 [arthas@pid]$ */
    private String prompt;

    public ShellImpl(ShellServer server, Term term, InternalCommandManager commandManager,
            Instrumentation instrumentation, long pid, JobControllerImpl jobController) {
        if (term instanceof TermImpl) {
            TermImpl termImpl = (TermImpl) term;
            TtyConnection conn = termImpl.getConn();
            // Telnet 本地连接：从 Netty Channel 提取 Principal 并完成 JAAS 登录
            if (conn instanceof TelnetTtyConnection) {
                TelnetConnection telnetConnection = ((TelnetTtyConnection) conn).getTelnetConnection();
                if (telnetConnection instanceof NettyTelnetConnection) {
                    ChannelHandlerContext handlerContext = ((NettyTelnetConnection) telnetConnection)
                            .channelHandlerContext();
                    Principal principal = AuthUtils.localPrincipal(handlerContext);
                    if (principal != null) {
                        try {
                            SecurityAuthenticator securityAuthenticator = ArthasBootstrap.getInstance().getSecurityAuthenticator();
                            Subject subject = securityAuthenticator.login(principal);
                            if (subject != null) {
                                session.put(ArthasConstants.SUBJECT_KEY, subject);
                            }
                        } catch (LoginException e) {
                            logger.error("local connection auth error", e);
                        }
                    }
                }
            }

            if (conn instanceof ExtHttpTtyConnection) {
                // HTTP TTY：将 Cookie 中的扩展 Session 属性复制到新 Session
                ExtHttpTtyConnection extConn = (ExtHttpTtyConnection) conn;
                Map<String, Object> extSessions = extConn.extSessions();
                for (Entry<String, Object> entry : extSessions.entrySet()) {
                    session.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (term != null && ARTHAS_AGENT_TERMINAL_TYPE.equalsIgnoreCase(term.type())) {
            session.put(Session.QUIET, Boolean.TRUE);
        }
        session.put(Session.COMMAND_MANAGER, commandManager);
        session.put(Session.INSTRUMENTATION, instrumentation);
        session.put(Session.PID, pid);
        session.put(Session.SERVER, server);
        session.put(Session.TTY, term);
        this.id = UUID.randomUUID().toString();
        session.put(Session.ID, id);
        this.commandManager = commandManager;
        this.closedFuture = Future.future();
        this.term = term;
        this.jobController = jobController;

        if (term != null) {
            term.setSession(session);
        }

        this.setPrompt();
    }

    /** @return 本会话绑定的 Job 控制器 */
    public JobController jobController() {
        return jobController;
    }

    /** @return 当前会话下所有活跃 Job 集合 */
    public Set<Job> jobs() {
        return jobController.jobs();
    }

    @Override
    /** 根据已分词的 CLI Token 创建并注册 Job */
    public synchronized Job createJob(List<CliToken> args) {
        Job job = jobController.createJob(commandManager, args, session, new ShellJobHandler(this), term, null);
        return job;
    }

    @Override
    /** 将整行命令分词后创建 Job */
    public Job createJob(String line) {
        return createJob(CliTokens.tokenize(line));
    }

    @Override
    /** @return 会话上下文对象 */
    public Session session() {
        return session;
    }

    /** @return 绑定的终端实例 */
    public Term term() {
        return term;
    }

    /** 包装 closedFuture 供 JobController 关闭回调使用 */
    public FutureHandler closedFutureHandler() {
        return new FutureHandler(closedFuture);
    }

    /** @return 终端最后活跃时间，用于会话超时回收 */
    public long lastAccessedTime() {
        return term.lastAccessedTime();
    }

    /** 设置连接时输出的欢迎语 */
    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }

    /** 根据 Session 中的 PID 生成 readline 提示符 */
    private void setPrompt(){
        this.prompt = "[arthas@" +
                session.getPid() +
                "]$ ";
    }

    /** 注册中断/挂起/关闭处理器，非静默会话时输出欢迎语 */
    public ShellImpl init() {
        term.interruptHandler(new InterruptHandler(this));
        term.suspendHandler(new SuspendHandler(this));
        term.closeHandler(new CloseHandler(this));

        if (!isQuietSession() && welcome != null && welcome.length() > 0) {
            term.write(welcome + "\n");
        }
        return this;
    }

    /** 是否静默会话（Agent 模式不输出欢迎信息） */
    private boolean isQuietSession() {
        return Boolean.TRUE.equals(session.get(Session.QUIET));
    }

    /** 格式化 jobs 命令的单条 Job 状态行（含执行次数、超时等） */
    public String statusLine(Job job, ExecStatus status) {
        StringBuilder sb = new StringBuilder("[").append(job.id()).append("]");
        if (this.session().equals(job.getSession())) {
            sb.append("*");
        }
        sb.append("\n");
        sb.append("       ").append(Character.toUpperCase(status.name().charAt(0)))
                .append(status.name().substring(1).toLowerCase());
        sb.append("           ").append(job.line()).append("\n");
        sb.append("       execution count : ").append(job.process().times()).append("\n");
        sb.append("       start time      : ").append(job.process().startTime()).append("\n");
        String cacheLocation = job.process().cacheLocation();
        if (cacheLocation != null) {
            sb.append("       cache location  : ").append(cacheLocation).append("\n");
        }
        Date timeoutDate = job.timeoutDate();
        if (timeoutDate != null) {
            sb.append("       timeout date    : ").append(timeoutDate).append("\n");
        }
        sb.append("       session         : ").append(job.getSession().getSessionId()).append(
                session.equals(job.getSession()) ? " (current)" : "").append("\n");
        return sb.toString();
    }

    /** 启动 readline 循环，绑定行处理器与命令补全 */
    public void readline() {
        term.readline(prompt, new ShellLineHandler(this),
                new CommandManagerCompletionHandler(commandManager));
    }

    /** 关闭会话：向终端写入原因并关闭连接，或无 term 时直接结束 JobController */
    public void close(String reason) {
        if (term != null) {
            try {
                term.write("session (" + session.getSessionId() + ") is closed because " + reason + "\n");
            } catch (Throwable t) {
                // WebSocket 关闭时偶发 NPE，捕获以保证 shutdown 流程完整（issue #320）
                // this ensures the shutdown process is finished properly
                // https://github.com/alibaba/arthas/issues/320
                logger.error("Error writing data:", t);
            }
            term.close();
        } else {
            jobController.close(closedFutureHandler());
        }
    }

    /** 记录当前前台 Job，供 jobs 命令标记 * */
    public void setForegroundJob(Job job) {
        currentForegroundJob = job;
    }

    /** @return 当前前台 Job，无则为 null */
    public Job getForegroundJob() {
        return currentForegroundJob;
    }

    /** Shell 侧 Job 生命周期监听：切换前台、Job 结束后恢复 readline 并保存历史 */
    private static class ShellJobHandler implements JobListener {
        ShellImpl shell;

        public ShellJobHandler(ShellImpl shell) {
            this.shell = shell;
        }

        @Override
        public void onForeground(Job job) {
            shell.setForegroundJob(job);
            // 前台 Job 可在此恢复 stdin 到 Job 原始处理器（当前未启用）
            //shell.term().stdinHandler(job.process().getStdinHandler());
        }

        @Override
        public void onBackground(Job job) {
            resetAndReadLine();
        }

        @Override
        public void onTerminated(Job job) {
            if (!job.isRunInBackground()){
                resetAndReadLine();
            }

            // Job 终止时持久化 readline 历史到磁盘
            Term term = shell.term();
            if (term instanceof TermImpl) {
                List<int[]> history = ((TermImpl) term).getReadline().getHistory();
                FileUtils.saveCommandHistory(history, new File(Constants.CMD_HISTORY_FILE));
            }
        }

        @Override
        public void onSuspend(Job job) {
            if (!job.isRunInBackground()){
                resetAndReadLine();
            }
        }

        /** 清空前台 Job 并重新进入 readline 等待输入 */
        private void resetAndReadLine() {
            // 恢复 stdin 为 echo 模式（当前未启用）
            //shell.term().stdinHandler(null);
            shell.setForegroundJob(null);
            shell.readline();
        }
    }

}
