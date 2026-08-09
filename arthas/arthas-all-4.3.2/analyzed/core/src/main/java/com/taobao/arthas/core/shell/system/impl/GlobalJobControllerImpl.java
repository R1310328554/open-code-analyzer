package com.taobao.arthas.core.shell.system.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.distribution.ResultDistributor;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.system.JobListener;
import com.taobao.arthas.core.shell.term.Term;


/**
 * 全局 Job 控制器：随 Arthas Agent 生命周期存在，不因单个 Shell 连接断开而销毁。
 * <p>
 * 在 {@link JobControllerImpl} 基础上为每个 Job 注册超时定时任务，
 * 到期自动 {@link Job#terminate()}，并解析 {@link GlobalOptions#jobTimeout} 配置。
 *
 * @author gehui 2017年7月31日 上午11:55:41
 */
public class GlobalJobControllerImpl extends JobControllerImpl {
    /** jobId → 超时定时任务，removeJob 时 cancel */
    private Map<Integer, JobTimeoutTask> jobTimeoutTaskMap = new ConcurrentHashMap<Integer, JobTimeoutTask>();
    private static final Logger logger = LoggerFactory.getLogger(GlobalJobControllerImpl.class);

    @Override
    /** 全局控制器不因 Shell 关闭而终止 Job，立即完成 completionHandler */
    public void close(final Handler<Void> completionHandler) {
        if (completionHandler != null) {
            completionHandler.handle(null);
        }
    }

    @Override
    /** 清理所有超时任务并终止全部 Job（Agent  shutdown 时调用） */
    public void close() {
        jobTimeoutTaskMap.clear();
        for (Job job : jobs()) {
            job.terminate();
        }
    }

    @Override
    /** 移除 Job 时同步取消对应的超时定时任务 */
    public boolean removeJob(int id) {
        JobTimeoutTask jobTimeoutTask = jobTimeoutTaskMap.remove(id);
        if (jobTimeoutTask != null) {
            jobTimeoutTask.cancel();
        }
        return super.removeJob(id);
    }

    @Override
    /** 创建 Job 并注册超时调度，将 timeoutDate 写入 Job */
    public Job createJob(InternalCommandManager commandManager, List<CliToken> tokens, Session session, JobListener jobHandler, Term term, ResultDistributor resultDistributor) {
        final Job job = super.createJob(commandManager, tokens, session, jobHandler, term, resultDistributor);

        /*
         * 达到超时时间将会停止 job
         */
        JobTimeoutTask jobTimeoutTask = new JobTimeoutTask(job);
        long jobTimeoutInSecond = getJobTimeoutInSecond();
        Date timeoutDate = new Date(System.currentTimeMillis() + (jobTimeoutInSecond * 1000));
        ArthasBootstrap.getInstance().getScheduledExecutorService().schedule(jobTimeoutTask, jobTimeoutInSecond, TimeUnit.SECONDS);
        jobTimeoutTaskMap.put(job.id(), jobTimeoutTask);
        job.setTimeoutDate(timeoutDate);

        return job;
    }

    /** 解析 {@link GlobalOptions#jobTimeout}，支持 h/d/m/s 后缀，失败时默认 1 天 */
    private long getJobTimeoutInSecond() {
        long result = -1;
        String jobTimeoutConfig = GlobalOptions.jobTimeout.trim();
        try {
            char unit = jobTimeoutConfig.charAt(jobTimeoutConfig.length() - 1);
            String duration = jobTimeoutConfig.substring(0, jobTimeoutConfig.length() - 1);
            switch (unit) {
            case 'h':
                result = TimeUnit.HOURS.toSeconds(Long.parseLong(duration));
                break;
            case 'd':
                result = TimeUnit.DAYS.toSeconds(Long.parseLong(duration));
                break;
            case 'm':
                result = TimeUnit.MINUTES.toSeconds(Long.parseLong(duration));
                break;
            case 's':
                result = Long.parseLong(duration);
                break;
            default:
                result = Long.parseLong(jobTimeoutConfig);
                break;
            }
        } catch (Throwable e) {
            logger.error("parse jobTimeoutConfig: {} error!", jobTimeoutConfig, e);
        }

        if (result < 0) {
            // 如果设置的属性有错误，那么使用默认的1天
            result = TimeUnit.DAYS.toSeconds(1);
            logger.warn("Configuration with job timeout " + jobTimeoutConfig + " is error, use 1d in default.");
        }
        return result;
    }

    /** Job 超时 Runnable：到期后 terminate 对应 Job */
    private static class JobTimeoutTask implements Runnable {
        private Job job;

        public JobTimeoutTask(Job job) {
            this.job = job;
        }

        @Override
        /** 超时触发：终止 Job 并清空引用防止重复执行 */
        public void run() {
            try {
                if (job != null) {
                    Job temp = job;
                    job = null;
                    temp.terminate();
                }
            } catch (Throwable e) {
                try {
                    logger.error("JobTimeoutTask error, job id: {}, line: {}", job.id(), job.line(), e);
                } catch (Throwable t) {
                    // ignore
                }
            }
        }

        /** 取消超时：Job 正常结束时由 removeJob 调用 */
        public void cancel() {
            job = null;
        }
    }
}
