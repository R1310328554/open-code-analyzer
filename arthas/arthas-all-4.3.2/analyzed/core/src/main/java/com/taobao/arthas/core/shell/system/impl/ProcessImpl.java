package com.taobao.arthas.core.shell.system.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.advisor.AdviceWeaver;
import com.taobao.arthas.core.command.basic1000.HelpCommand;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.command.model.StatusModel;
import com.taobao.arthas.core.distribution.ResultDistributor;
import com.taobao.arthas.core.distribution.impl.TermResultDistributorImpl;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.command.internal.CloseFunction;
import com.taobao.arthas.core.shell.command.internal.StatisticsFunction;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.system.ExecStatus;
import com.taobao.arthas.core.shell.system.Process;
import com.taobao.arthas.core.shell.system.ProcessAware;
import com.taobao.arthas.core.shell.term.Tty;
import com.taobao.middleware.cli.CLIException;
import com.taobao.middleware.cli.CommandLine;
import io.termd.core.function.Function;

import java.lang.instrument.ClassFileTransformer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link Process} 默认实现：驱动单条 Arthas 命令的完整生命周期。
 * <p>
 * 内部维护 {@link CommandProcessImpl} 供命令 handler 交互，管理 TTY 前后台切换、
 * AdviceWeaver 注册/注销及 stdout handler 链输出。
 *
 * @author beiwei30 on 10/11/2016.
 * @author gongdewei 2020-03-26
 */
public class ProcessImpl implements Process {

    private static final Logger logger = LoggerFactory.getLogger(ProcessImpl.class);

    /** 命令定义（含 CLI 解析器与 processHandler） */
    private Command commandContext;
    /** 命令执行入口 handler */
    private Handler<CommandProcess> handler;
    /** 命令参数 token 列表 */
    private List<CliToken> args;
    /** 关联终端 */
    private Tty tty;
    /** Shell 会话 */
    private Session session;
    /** 中断信号回调 */
    private Handler<Void> interruptHandler;
    /** 挂起回调 */
    private Handler<Void> suspendHandler;
    /** 恢复回调 */
    private Handler<Void> resumeHandler;
    /** 正常结束回调 */
    private Handler<Void> endHandler;
    /** 切后台回调 */
    private Handler<Void> backgroundHandler;
    /** 切前台回调 */
    private Handler<Void> foregroundHandler;
    /** 进程终止回调（传递 exitCode） */
    private Handler<Integer> terminatedHandler;
    /** 逻辑前台标志（CommandProcess 可见） */
    private boolean foreground;
    /** 当前执行状态 */
    private volatile ExecStatus processStatus;
    /** 是否占用 TTY 前台（控制 stdin/resize） */
    private boolean processForeground;
    /** 标准输入 handler */
    private Handler<String> stdinHandler;
    /** 终端 resize handler */
    private Handler<Void> resizeHandler;
    /** 退出码 */
    private Integer exitCode;
    /** 命令运行时上下文 */
    private CommandProcessImpl process;
    /** 启动时间 */
    private Date startTime;
    /** stdout 输出链封装 */
    private ProcessOutput processOutput;
    /** 所属 Job id */
    private int jobId;
    /** 结构化结果分发器 */
    private ResultDistributor resultDistributor;

    /**
     * @param commandContext 命令定义
     * @param args 参数 token
     * @param handler 命令 processHandler
     * @param processOutput stdout handler 链
     * @param resultDistributor 结果分发器，可为 null
     */
    public ProcessImpl(Command commandContext, List<CliToken> args, Handler<CommandProcess> handler,
                       ProcessOutput processOutput, ResultDistributor resultDistributor) {
        this.commandContext = commandContext;
        this.handler = handler;
        this.args = args;
        this.resultDistributor = resultDistributor;
        this.processStatus = ExecStatus.READY;
        this.processOutput = processOutput;
    }

    @Override
    /** @return 进程退出码 */
    public Integer exitCode() {
        return exitCode;
    }

    @Override
    /** @return 当前 {@link ExecStatus} */
    public ExecStatus status() {
        return processStatus;
    }

    @Override
    /** 绑定 TTY 终端 */
    public synchronized Process setTty(Tty tty) {
        this.tty = tty;
        return this;
    }

    @Override
    /** @return 关联 TTY */
    public synchronized Tty getTty() {
        return tty;
    }

    @Override
    /** 设置所属 Job id */
    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    @Override
    /** 绑定 Shell Session */
    public synchronized Process setSession(Session session) {
        this.session = session;
        return this;
    }

    @Override
    /** @return Shell Session */
    public synchronized Session getSession() {
        return session;
    }

    @Override
    /** @return 命令执行次数 */
    public int times() {
        return process.times().get();
    }

    /** @return 进程启动时间 */
    public Date startTime() {
        return startTime;
    }

    @Override
    /** @return 重定向缓存路径 */
    public String cacheLocation() {
        if (processOutput != null) {
            return processOutput.cacheLocation;
        }
        return null;
    }

    @Override
    /** 注册终止回调 */
    public Process terminatedHandler(Handler<Integer> handler) {
        terminatedHandler = handler;
        return this;
    }

    @Override
    /** 中断进程（无完成回调） */
    public boolean interrupt() {
        return interrupt(null);
    }

    @Override
    /** 中断进程并在完成后回调 */
    public boolean interrupt(final Handler<Void> completionHandler) {
        if (processStatus == ExecStatus.RUNNING || processStatus == ExecStatus.STOPPED || processStatus == ExecStatus.TERMINATED) {
            final Handler<Void> handler = interruptHandler;
            try {
                if (handler != null) {
                    handler.handle(null);
                }
            } finally {
                if (completionHandler != null) {
                    completionHandler.handle(null);
                }
            }
            return handler != null;
        } else {
            throw new IllegalStateException("Cannot interrupt process in " + processStatus + " state");
        }
    }

    @Override
    public void resume() {
        resume(true);
    }

    @Override
    public void resume(boolean foreground) {
        resume(foreground, null);
    }

    @Override
    public void resume(Handler<Void> completionHandler) {
        resume(true, completionHandler);
    }

    @Override
    /** 从 STOPPED 恢复为 RUNNING */
    public synchronized void resume(boolean fg, Handler<Void> completionHandler) {
        if (processStatus == ExecStatus.STOPPED) {
            updateStatus(ExecStatus.RUNNING, null, fg, resumeHandler, terminatedHandler, completionHandler);
            if (process != null) {
                process.resume();
            }
        } else {
            throw new IllegalStateException("Cannot resume process in " + processStatus + " state");
        }
    }

    @Override
    public void suspend() {
        suspend(null);
    }

    @Override
    /** 挂起 RUNNING 进程为 STOPPED */
    public synchronized void suspend(Handler<Void> completionHandler) {
        if (processStatus == ExecStatus.RUNNING) {
            updateStatus(ExecStatus.STOPPED, null, false, suspendHandler, terminatedHandler, completionHandler);
            if (process != null) {
                process.suspend();
            }
        } else {
            throw new IllegalStateException("Cannot suspend process in " + processStatus + " state");
        }
    }

    @Override
    public void toBackground() {
        toBackground(null);
    }

    @Override
    /** 运行中进程切到后台，释放 TTY stdin */
    public void toBackground(Handler<Void> completionHandler) {
        if (processStatus == ExecStatus.RUNNING) {
            if (processForeground) {
                updateStatus(ExecStatus.RUNNING, null, false, backgroundHandler, terminatedHandler, completionHandler);
            }
        } else {
            throw new IllegalStateException("Cannot set to background a process in " + processStatus + " state");
        }
    }

    @Override
    public void toForeground() {
        toForeground(null);
    }

    @Override
    /** 后台进程切到前台，绑定 stdin/resize */
    public void toForeground(Handler<Void> completionHandler) {
        if (processStatus == ExecStatus.RUNNING) {
            if (!processForeground) {
                updateStatus(ExecStatus.RUNNING, null, true, foregroundHandler, terminatedHandler, completionHandler);
            }
        } else {
            throw new IllegalStateException("Cannot set to foreground a process in " + processStatus + " state");
        }
    }

    @Override
    public void terminate() {
        terminate(null);
    }

    @Override
    /** 终止进程并触发 terminatedHandler */
    public void terminate(Handler<Void> completionHandler) {
        if (!terminate(-10, completionHandler, null)) {
            throw new IllegalStateException("Cannot terminate terminated process");
        }
    }

    /** 内部终止逻辑：写 StatusModel、close 输出、unregister Advice */
    private synchronized boolean terminate(int exitCode, Handler<Void> completionHandler, String message) {
        if (processStatus != ExecStatus.TERMINATED) {
            //add status message
            this.appendResult(new StatusModel(exitCode, message));
            if (process != null) {
                processOutput.close();
            }
            updateStatus(ExecStatus.TERMINATED, exitCode, false, endHandler, terminatedHandler, completionHandler);
            if (process != null) {
                process.unregister();
            }
            return true;
        } else {
            return false;
        }
    }

    /** 附加结构化结果并设置 jobId */
    private void appendResult(ResultModel result) {
        result.setJobId(jobId);
        if (resultDistributor != null) {
            resultDistributor.appendResult(result);
        }
    }

    /** 统一状态迁移：更新 TTY 绑定并依次调用 lifecycle handler */
    private void updateStatus(ExecStatus statusUpdate, Integer exitCodeUpdate, boolean foregroundUpdate,
                              Handler<Void> handler, Handler<Integer> terminatedHandler,
                              Handler<Void> completionHandler) {
        processStatus = statusUpdate;
        exitCode = exitCodeUpdate;
        if (!foregroundUpdate) {
            if (processForeground) {
                processForeground = false;
                if (stdinHandler != null) {
                    tty.stdinHandler(null);
                }
                if (resizeHandler != null) {
                    tty.resizehandler(null);
                }
            }
        } else {
            if (!processForeground) {
                processForeground = true;
                if (stdinHandler != null) {
                    tty.stdinHandler(stdinHandler);
                }
                if (resizeHandler != null) {
                    tty.resizehandler(resizeHandler);
                }
            }
        }

        foreground = foregroundUpdate;
        try {
            if (handler != null) {
                handler.handle(null);
            }
        } finally {
            if (completionHandler != null) {
                completionHandler.handle(null);
            }
            if (terminatedHandler != null && statusUpdate == ExecStatus.TERMINATED) {
                terminatedHandler.handle(exitCodeUpdate);
            }
        }
    }

    @Override
    public void run() {
        run(true);
    }

    @Override
    /** 启动命令：解析 CLI、创建 CommandProcessImpl 并提交线程池执行 */
    public synchronized void run(boolean fg) {
        if (processStatus != ExecStatus.READY) {
            throw new IllegalStateException("Cannot run proces in " + processStatus + " state");
        }

        processStatus = ExecStatus.RUNNING;
        processForeground = fg;
        foreground = fg;
        startTime = new Date();

        // Make a local copy
        final Tty tty = this.tty;
        if (tty == null) {
            throw new IllegalStateException("Cannot execute process without a TTY set");
        }

        process = new CommandProcessImpl(this, tty);
        if (resultDistributor == null) {
            resultDistributor = new TermResultDistributorImpl(process, ArthasBootstrap.getInstance().getResultViewResolver());
        }

        final List<String> args2 = new LinkedList<String>();
        for (CliToken arg : args) {
            if (arg.isText()) {
                args2.add(arg.value());
            }
        }

        CommandLine cl = null;
        try {
            if (commandContext.cli() != null) {
                if (commandContext.cli().parse(args2, false).isAskingForHelp()) {
                    appendResult(new HelpCommand().createHelpDetailModel(commandContext));
                    terminate();
                    return;
                }

                cl = commandContext.cli().parse(args2);
                process.setArgs2(args2);
                process.setCommandLine(cl);
            }
        } catch (CLIException e) {
            terminate(-10, null, e.getMessage());
            return;
        }

        if (cacheLocation() != null) {
            process.echoTips("job id  : " + this.jobId + "\n");
            process.echoTips("cache location  : " + cacheLocation() + "\n");
        }
        Runnable task = new CommandProcessTask(process);
        ArthasBootstrap.getInstance().execute(task);
    }

    /** 在线程池中执行命令 handler 的任务 */
    private class CommandProcessTask implements Runnable {

        private CommandProcess process;

        public CommandProcessTask(CommandProcess process) {
            this.process = process;
        }

        @Override
        /** 调用命令 handler；异常时 end(1) 并提示查看日志 */
        public void run() {
            try {
                handler.handle(process);
            } catch (Throwable t) {
                logger.error("Error during processing the command:", t);
                process.end(1, "Error during processing the command: " + t.getClass().getName() + ", message:" + t.getMessage()
                        + ", please check $HOME/logs/arthas/arthas.log for more details." );
            }
        }
    }

    /** {@link CommandProcess} 实现：命令 handler 与 Process/TTY 的交互面 */
    private class CommandProcessImpl implements CommandProcess {

        private final Process process;
        private final Tty tty;
        /** 解析后的字符串参数 */
        private List<String> args2;
        /** middleware-cli 解析结果 */
        private CommandLine commandLine;
        /** 命令执行轮次计数（watch/trace 等） */
        private AtomicInteger times = new AtomicInteger();
        /** 已注册的 AdviceListener */
        private AdviceListener listener = null;
        /** 关联的 ClassFileTransformer */
        private ClassFileTransformer transformer;

        public CommandProcessImpl(Process process, Tty tty) {
            this.process = process;
            this.tty = tty;
        }

        @Override
        public List<CliToken> argsTokens() {
            return args;
        }

        @Override
        public List<String> args() {
            return args2;
        }

        @Override
        public String type() {
            return tty.type();
        }

        @Override
        public boolean isForeground() {
            return foreground;
        }

        @Override
        public int width() {
            return tty.width();
        }

        @Override
        public int height() {
            return tty.height();
        }

        @Override
        public CommandLine commandLine() {
            return commandLine;
        }

        @Override
        public Session session() {
            return session;
        }

        @Override
        public AtomicInteger times() {
            return times;
        }

        public void setArgs2(List<String> args2) {
            this.args2 = args2;
        }

        public void setCommandLine(CommandLine commandLine) {
            this.commandLine = commandLine;
        }

        @Override
        public CommandProcess stdinHandler(Handler<String> handler) {
            stdinHandler = handler;
            if (processForeground && stdinHandler != null) {
                tty.stdinHandler(stdinHandler);
            }
            return this;
        }

        @Override
        /** 经 stdout handler 链写出文本 */
        public CommandProcess write(String data) {
            if (processStatus != ExecStatus.RUNNING) {
                throw new IllegalStateException(
                        "Cannot write to standard output when " + status().name().toLowerCase());
            }
            processOutput.write(data);
            return this;
        }

        @Override
        public void echoTips(String tips) {
            processOutput.term.write(tips);
        }

        @Override
        public String cacheLocation() {
            return ProcessImpl.this.cacheLocation();
        }

        @Override
        public CommandProcess resizehandler(Handler<Void> handler) {
            resizeHandler = handler;
            tty.resizehandler(resizeHandler);
            return this;
        }

        @Override
        public CommandProcess interruptHandler(Handler<Void> handler) {
            synchronized (ProcessImpl.this) {
                interruptHandler = handler;
            }
            return this;
        }

        @Override
        public CommandProcess suspendHandler(Handler<Void> handler) {
            synchronized (ProcessImpl.this) {
                suspendHandler = handler;
            }
            return this;
        }

        @Override
        public CommandProcess resumeHandler(Handler<Void> handler) {
            synchronized (ProcessImpl.this) {
                resumeHandler = handler;
            }
            return this;
        }

        @Override
        public CommandProcess endHandler(Handler<Void> handler) {
            synchronized (ProcessImpl.this) {
                endHandler = handler;
            }
            return this;
        }

        @Override
        public CommandProcess backgroundHandler(Handler<Void> handler) {
            synchronized (ProcessImpl.this) {
                backgroundHandler = handler;
            }
            return this;
        }

        @Override
        public CommandProcess foregroundHandler(Handler<Void> handler) {
            synchronized (ProcessImpl.this) {
                foregroundHandler = handler;
            }
            return this;
        }

        @Override
        /** 注册 Advice 监听与 transformer，ProcessAware 自动绑定 Process */
        public void register(AdviceListener adviceListener, ClassFileTransformer transformer) {
            if (adviceListener instanceof ProcessAware) {
                ProcessAware processAware = (ProcessAware) adviceListener;
                // listener 有可能是其它 command 创建的
                if(processAware.getProcess() == null) {
                    processAware.setProcess(this.process);
                }
            }
            this.listener = adviceListener;
            AdviceWeaver.reg(listener);
            
            this.transformer = transformer;
        }

        @Override
        /** 移除 transformer 并按 ProcessAware 规则 unReg Advice */
        public void unregister() {
            if (transformer != null) {
                ArthasBootstrap.getInstance().getTransformerManager().removeTransformer(transformer);
            }
            
            if (listener instanceof ProcessAware) {
                // listener有可能其它 command 创建的，所以不能unRge
                if (this.process.equals(((ProcessAware) listener).getProcess())) {
                    AdviceWeaver.unReg(listener);
                }
            } else {
                AdviceWeaver.unReg(listener);
            }
        }

        @Override
        public void resume() {
//            if (suspendedListener != null) {
//                AdviceWeaver.resume(suspendedListener);
//                suspendedListener = null;
//            }
        }

        @Override
        public void suspend() {
//            if (this.enhanceLock >= 0) {
//                suspendedListener = AdviceWeaver.suspend(enhanceLock);
//            }
        }

        @Override
        public void end() {
            end(0);
        }

        @Override
        public void end(int statusCode) {
            end(statusCode, null);
        }

        @Override
        public void end(int statusCode, String message) {
            terminate(statusCode, null, message);
        }

        @Override
        public boolean isRunning() {
            return processStatus == ExecStatus.RUNNING;
        }

        @Override
        /** 追加结构化结果到 ResultDistributor */
        public void appendResult(ResultModel result) {
            if (processStatus != ExecStatus.RUNNING) {
                throw new IllegalStateException(
                        "Cannot write to standard output when " + status().name().toLowerCase());
            }
            ProcessImpl.this.appendResult(result);
        }
    }

    /** 命令 stdout 输出链：经 Function 链处理后写入终端或文件 */
    static class ProcessOutput {

        /** 实时输出 handler 链（至 StatisticsFunction 为止） */
        private List<Function<String, String>> stdoutHandlerChain;
        /** 统计类 handler（close 时 flush 汇总结果） */
        private StatisticsFunction statisticsHandler = null;
        /** Statistics 之后的 flush 链 */
        private List<Function<String, String>> flushHandlerChain = null;
        /** 重定向缓存路径 */
        private String cacheLocation;
        /** 终端引用（echoTips 等） */
        private Tty term;

        /**
         * 拆分 handler 链：StatisticsFunction 之前为实时输出，之后为 close 时 flush。
         */
        public ProcessOutput(List<Function<String, String>> stdoutHandlerChain, String cacheLocation, Tty term) {
            // this.stdoutHandlerChain = stdoutHandlerChain;

            int i = 0;
            for (; i < stdoutHandlerChain.size(); i++) {
                if (stdoutHandlerChain.get(i) instanceof StatisticsFunction) {
                    break;
                }
            }
            if (i < stdoutHandlerChain.size()) {
                this.stdoutHandlerChain = stdoutHandlerChain.subList(0, i + 1);
                this.statisticsHandler = (StatisticsFunction) stdoutHandlerChain.get(i);
                if (i < stdoutHandlerChain.size() - 1) {
                    flushHandlerChain = stdoutHandlerChain.subList(i + 1, stdoutHandlerChain.size());
                }
            } else {
                this.stdoutHandlerChain = stdoutHandlerChain;
            }

            this.cacheLocation = cacheLocation;
            this.term = term;
        }

        /** 逐 handler 变换并输出数据 */
        private void write(String data) {
            if (stdoutHandlerChain != null) {
                //hotspot, reduce memory fragment (foreach/iterator)
                int size = stdoutHandlerChain.size();
                for (int i = 0; i < size; i++) {
                    Function<String, String> function = stdoutHandlerChain.get(i);
                    data = function.apply(data);
                }
            }
        }

        /** 关闭输出链：flush 统计结果并调用 CloseFunction */
        private void close() {
            if (statisticsHandler != null && flushHandlerChain != null) {
                String data = statisticsHandler.result();

                for (Function<String, String> function : flushHandlerChain) {
                    data = function.apply(data);
                    if (function instanceof StatisticsFunction) {
                        data = ((StatisticsFunction) function).result();
                    }
                }
            }

            if (stdoutHandlerChain != null) {
                for (Function<String, String> function : stdoutHandlerChain) {
                    if (function instanceof CloseFunction) {
                        ((CloseFunction) function).close();
                    }
                }
            }
        }
    }
}
