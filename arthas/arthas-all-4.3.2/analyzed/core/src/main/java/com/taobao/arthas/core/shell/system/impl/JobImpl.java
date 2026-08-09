package com.taobao.arthas.core.shell.system.impl;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.system.ExecStatus;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.system.JobListener;
import com.taobao.arthas.core.shell.system.Process;

/**
 * {@link Job} 默认实现：包装 {@link Process}，协调前台/后台切换与 {@link JobListener} 回调。
 * <p>
 * Process 终止时通过 {@link TerminatedHandler} 通知 listener 并完成 {@link #terminateFuture}。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author hengyunabc 2019-05-14
 * @author gongdewei 2020-03-23
 */
public class JobImpl implements Job {

    /** Job 在控制器内的唯一 id */
    final int id;
    /** 所属 JobController */
    final JobControllerImpl controller;
    /** 关联的命令进程 */
    final Process process;
    /** 完整命令行字符串 */
    final String line;
    /** 所属 Shell 会话 */
    private volatile Session session;
    /** 内部测试用：Process 实际状态镜像 */
    private volatile ExecStatus actualStatus; // Used internally for testing only
    /** 上次停止时间戳 */
    volatile long lastStopped; // When the job was last stopped
    /** Job 生命周期 listener（Shell 层） */
    volatile JobListener jobHandler;
    /** 可选的状态变更通知 handler */
    volatile Handler<ExecStatus> statusUpdateHandler;
    /** Job 超时截止时刻 */
    volatile Date timeoutDate;
    /** Process 终止时完成的 Future */
    final Future<Void> terminateFuture;
    /** 是否在后台运行 */
    final AtomicBoolean runInBackground;
    //final Handler<Job> foregroundUpdatedHandler;

    /**
     * @param id job id
     * @param controller 所属控制器
     * @param process 命令进程
     * @param line 命令行
     * @param runInBackground 是否后台启动
     * @param session Shell 会话
     * @param jobHandler 生命周期 listener
     */
    JobImpl(int id, final JobControllerImpl controller, Process process, String line, boolean runInBackground,
            Session session, JobListener jobHandler) {
        this.id = id;
        this.controller = controller;
        this.process = process;
        this.line = line;
        this.session = session;
        this.terminateFuture = Future.future();
        this.runInBackground = new AtomicBoolean(runInBackground);
        this.jobHandler = jobHandler;
        if (jobHandler == null) {
            throw new IllegalArgumentException("JobListener is required");
        }
        //this.foregroundUpdatedHandler = new ShellForegroundUpdateHandler(shell);
        process.terminatedHandler(new TerminatedHandler(controller));
    }

    /** @return 内部测试用的 actualStatus */
    public ExecStatus actualStatus() {
        return actualStatus;
    }

    @Override
    /** 转发中断到 Process */
    public boolean interrupt() {
        return process.interrupt();
    }

    @Override
    /** 恢复 Job 并默认切到前台 */
    public Job resume() {
        return resume(true);
    }

    @Override
    /** @return Job 超时时间点 */
    public Date timeoutDate() {
        return timeoutDate;
    }

    @Override
    /** 设置 Job 超时截止时刻 */
    public void setTimeoutDate(Date date) {
        this.timeoutDate = date;
    }

    @Override
    /** @return 所属 Session */
    public Session getSession() {
        return session;
    }

    @Override
    /** 恢复 Job：更新前后台标志并通知 listener */
    public Job resume(boolean foreground) {
        try {
            process.resume(foreground, new ResumeHandler());
        } catch (IllegalStateException ignore) {

        }

        runInBackground.set(!foreground);

//        if (foreground) {
//            if (foregroundUpdatedHandler != null) {
//                foregroundUpdatedHandler.handle(this);
//            }
//        }
        if (statusUpdateHandler != null) {
            statusUpdateHandler.handle(process.status());
        }

        if (this.status() == ExecStatus.RUNNING) {
            if (foreground) {
                jobHandler.onForeground(this);
            } else {
                jobHandler.onBackground(this);
            }
        }
        return this;
    }

    @Override
    /** 挂起 Job 并通知 listener */
    public Job suspend() {
        try {
            process.suspend(new SuspendHandler());
        } catch (IllegalStateException ignore) {
            return this;
        }
//        if (!runInBackground.get() && foregroundUpdatedHandler != null) {
//            foregroundUpdatedHandler.handle(null);
//        }
        if (statusUpdateHandler != null) {
            statusUpdateHandler.handle(process.status());
        }

//        shell.setForegroundJob(null);
        jobHandler.onSuspend(this);
        return this;
    }

    @Override
    /** 终止 Process 并从 controller 移除 */
    public void terminate() {
        try {
            process.terminate();
        } catch (IllegalStateException ignore) {
            // Process already terminated, likely by itself
        } finally {
            controller.removeJob(this.id);
        }
    }

    @Override
    /** @return 关联 Process */
    public Process process() {
        return process;
    }

    /** @return Process 当前状态 */
    public ExecStatus status() {
        return process.status();
    }

    /** @return 完整命令行 */
    public String line() {
        return line;
    }

    @Override
    /** @return 是否在后台运行 */
    public boolean isRunInBackground() {
        return runInBackground.get();
    }

    @Override
    /** 切到后台：CAS 更新 runInBackground 并通知 listener */
    public Job toBackground() {
        if (!this.runInBackground.get()) {
            // run in foreground mode
            if (runInBackground.compareAndSet(false, true)) {
                process.toBackground();
                if (statusUpdateHandler != null) {
                    statusUpdateHandler.handle(process.status());
                }
                jobHandler.onBackground(this);
            }
        }

//        shell.setForegroundJob(null);
//        jobHandler.onBackground(this);
        return this;
    }

    @Override
    /** 切到前台：CAS 更新并通知 listener */
    public Job toForeground() {
        if (this.runInBackground.get()) {
            if (runInBackground.compareAndSet(true, false)) {
//                if (foregroundUpdatedHandler != null) {
//                    foregroundUpdatedHandler.handle(this);
//                }
                process.toForeground();
                if (statusUpdateHandler != null) {
                    statusUpdateHandler.handle(process.status());
                }

//                shell.setForegroundJob(this);
                jobHandler.onForeground(this);
            }
        }

        return this;
    }

    @Override
    /** @return Job id */
    public int id() {
        return id;
    }

    @Override
    /** 按 runInBackground 标志决定前台/后台运行 */
    public Job run() {
        return run(!runInBackground.get());
    }

    @Override
    /** 启动 Process 并通知 listener 前后台状态 */
    public Job run(boolean foreground) {
//        if (foreground && foregroundUpdatedHandler != null) {
//            foregroundUpdatedHandler.handle(this);
//        }

        actualStatus = ExecStatus.RUNNING;
        if (statusUpdateHandler != null) {
            statusUpdateHandler.handle(ExecStatus.RUNNING);
        }
        //set process's tty in JobControllerImpl.createCommandProcess
        //process.setTty(shell.term());
        process.setSession(this.session);
        process.run(foreground);

//        if (!foreground && foregroundUpdatedHandler != null) {
//            foregroundUpdatedHandler.handle(null);
//        }
//
//        if (foreground) {
//            shell.setForegroundJob(this);
//        } else {
//            shell.setForegroundJob(null);
//        }
        if (this.status() == ExecStatus.RUNNING) {
            if (foreground) {
                jobHandler.onForeground(this);
            } else {
                jobHandler.onBackground(this);
            }
        }
        return this;
    }

    /** Process 终止回调：通知 listener、移除 Job、完成 terminateFuture */
    private class TerminatedHandler implements Handler<Integer> {

        private final JobControllerImpl controller;

        public TerminatedHandler(JobControllerImpl controller) {
            this.controller = controller;
        }

        @Override
        public void handle(Integer exitCode) {
//            if (!runInBackground.get() && actualStatus.equals(ExecStatus.RUNNING)) {
                // 只有前台在运行的任务，才需要调用foregroundUpdateHandler
//                if (foregroundUpdatedHandler != null) {
//                    foregroundUpdatedHandler.handle(null);
//                }
//            }
            jobHandler.onTerminated(JobImpl.this);
            controller.removeJob(JobImpl.this.id);
            if (statusUpdateHandler != null) {
                statusUpdateHandler.handle(ExecStatus.TERMINATED);
            }
            terminateFuture.complete();

            // save command history (move to JobControllerImpl.ShellJobHandler.onTerminated)
//            Term term = shell.term();
//            if (term instanceof TermImpl) {
//                List<int[]> history = ((TermImpl) term).getReadline().getHistory();
//                FileUtils.saveCommandHistory(history, new File(Constants.CMD_HISTORY_FILE));
//            }
        }
    }

    /** resume 完成时更新 actualStatus 为 RUNNING */
    private class ResumeHandler implements Handler<Void> {

        @Override
        public void handle(Void event) {
            actualStatus = ExecStatus.RUNNING;
        }
    }

    /** suspend 完成时更新 actualStatus 为 STOPPED */
    private class SuspendHandler implements Handler<Void> {

        @Override
        public void handle(Void event) {
            actualStatus = ExecStatus.STOPPED;
        }
    }
}
