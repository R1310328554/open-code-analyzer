package com.taobao.arthas.core.shell.system.impl;

import com.taobao.arthas.common.ArthasConstants;
import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.distribution.ResultDistributor;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.internal.RedirectHandler;
import com.taobao.arthas.core.shell.command.internal.StdoutHandler;
import com.taobao.arthas.core.shell.command.internal.TermHandler;
import com.taobao.arthas.core.shell.future.Future;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.system.JobController;
import com.taobao.arthas.core.shell.system.JobListener;
import com.taobao.arthas.core.shell.system.Process;
import com.taobao.arthas.core.shell.system.impl.ProcessImpl.ProcessOutput;
import com.taobao.arthas.core.shell.term.Term;
import com.taobao.arthas.core.util.Constants;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.TokenUtils;

import io.termd.core.function.Function;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link JobController} 默认实现：管理 Shell 会话内的 Job 集合，负责创建、关闭与权限校验。
 * <p>
 * 解析命令行 token 构建 {@link ProcessImpl}，支持管道 {@code |}、重定向 {@code >}/{@code >>}、
 * 后台 {@code &} 及 stdout handler 链组装。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author hengyunabc 2019-05-14
 * @author gongdewei 2020-03-23
 */
public class JobControllerImpl implements JobController {

    /** 按 jobId 排序的活跃 Job 表 */
    private final SortedMap<Integer, JobImpl> jobs = new TreeMap<Integer, JobImpl>();
    /** Job id 自增生成器 */
    private final AtomicInteger idGenerator = new AtomicInteger(0);
    /** 控制器是否已关闭 */
    private boolean closed = false;

    public JobControllerImpl() {
    }

    /** @return 当前所有 Job 的快照集合 */
    public synchronized Set<Job> jobs() {
        return new HashSet<Job>(jobs.values());
    }

    /** @param id Job 数字 id */
    /** @return 指定 id 的 Job，不存在时为 null */
    public synchronized Job getJob(int id) {
        return jobs.get(id);
    }

    /** 从控制器移除 Job；子类可在移除前做额外清理 */
    synchronized boolean removeJob(int id) {
        return jobs.remove(id) != null;
    }

    /** 校验 Session 是否已通过 auth；未登录时仅允许 auth 命令 */
    private void checkPermission(Session session, CliToken token) {
        if (ArthasBootstrap.getInstance().getSecurityAuthenticator().needLogin()) {
            // 检查session是否有 Subject
            Object subject = session.get(ArthasConstants.SUBJECT_KEY);
            if (subject == null) {
                if (token != null && token.isText() && token.value().trim().equals(ArthasConstants.AUTH)) {
                    // 执行的是auth 命令
                    return;
                }
                throw new IllegalArgumentException("Error! command not permitted, try to use 'auth' command to authenticates.");
            }
        }
    }

    @Override
    /** 解析 token 创建 Job 与 Process，注册到 jobs 表 */
    public Job createJob(InternalCommandManager commandManager, List<CliToken> tokens, Session session, JobListener jobHandler, Term term, ResultDistributor resultDistributor) {
        checkPermission(session, tokens.get(0));
        int jobId = idGenerator.incrementAndGet();
        StringBuilder line = new StringBuilder();
        for (CliToken arg : tokens) {
            line.append(arg.raw());
        }
        boolean runInBackground = runInBackground(tokens);
        Process process = createProcess(session, tokens, commandManager, jobId, term, resultDistributor);
        process.setJobId(jobId);
        JobImpl job = new JobImpl(jobId, this, process, line.toString(), runInBackground, session, jobHandler);
        jobs.put(jobId, job);
        return job;
    }

    /** 统计当前将结果重定向到缓存文件的 Job 数量（上限 8） */
    private int getRedirectJobCount() {
        int count = 0;
        for (Job job : jobs.values()) {
            if (job.process() != null && job.process().cacheLocation() != null) {
                count++;
            }
        }
        return count;
    }

    @Override
    /** 关闭控制器：终止全部 Job，全部 terminateFuture 完成后回调 */
    public void close(final Handler<Void> completionHandler) {
        List<JobImpl> jobs;
        synchronized (this) {
            if (closed) {
                jobs = Collections.emptyList();
            } else {
                jobs = new ArrayList<JobImpl>(this.jobs.values());
                closed = true;
            }
        }
        if (jobs.isEmpty()) {
            if (completionHandler!= null) {
                completionHandler.handle(null);
            }
        } else {
            final AtomicInteger count = new AtomicInteger(jobs.size());
            for (JobImpl job : jobs) {
                job.terminateFuture.setHandler(new Handler<Future<Void>>() {
                    @Override
                    public void handle(Future<Void> v) {
                        if (count.decrementAndGet() == 0 && completionHandler != null) {
                            completionHandler.handle(null);
                        }
                    }
                });
                job.terminate();
            }
        }
    }

    /**
     * 从 CLI token 列表创建 {@link Process}：取首个文本 token 作为命令名。
     *
     * @param line 命令行 token 列表
     * @param commandManager 命令管理器
     * @param jobId 分配的 job id
     * @param term 终端
     * @param resultDistributor 结果分发器
     * @return 创建的 Process
     */
    private Process createProcess(Session session, List<CliToken> line, InternalCommandManager commandManager, int jobId, Term term, ResultDistributor resultDistributor) {
        try {
            ListIterator<CliToken> tokens = line.listIterator();
            while (tokens.hasNext()) {
                CliToken token = tokens.next();
                if (token.isText()) {
                    // check before create process
                    checkPermission(session, token);
                    Command command = commandManager.getCommand(token.value());
                    if (command != null) {
                        return createCommandProcess(command, tokens, jobId, term, resultDistributor);
                    } else {
                        throw new IllegalArgumentException(token.value() + ": command not found");
                    }
                }
            }
            throw new IllegalArgumentException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 检测行尾 {@code &} 并移除，表示后台运行 */
    private boolean runInBackground(List<CliToken> tokens) {
        boolean runInBackground = false;
        CliToken last = TokenUtils.findLastTextToken(tokens);
        if (last != null && "&".equals(last.value())) {
            runInBackground = true;
            tokens.remove(last);
        }
        return runInBackground;
    }

    /**
     * 构建命令 Process：解析管道与重定向，组装 stdout handler 链。
     */
    private Process createCommandProcess(Command command, ListIterator<CliToken> tokens, int jobId, Term term, ResultDistributor resultDistributor) throws IOException {
        List<CliToken> remaining = new ArrayList<CliToken>();
        List<CliToken> pipelineTokens = new ArrayList<CliToken>();
        boolean isPipeline = false;
        RedirectHandler redirectHandler = null;
        List<Function<String, String>> stdoutHandlerChain = new ArrayList<Function<String, String>>();
        String cacheLocation = null;
        while (tokens.hasNext()) {
            CliToken remainingToken = tokens.next();
            if (remainingToken.isText()) {
                String tokenValue = remainingToken.value();
                if ("|".equals(tokenValue)) {
                    isPipeline = true;
                    // 将管道符|之后的部分注入为输出链上的handler
                    injectHandler(stdoutHandlerChain, pipelineTokens);
                    continue;
                } else if (">>".equals(tokenValue) || ">".equals(tokenValue)) {
                    String name = getRedirectFileName(tokens);
                    if (name == null) {
                        // 如果没有指定重定向文件名，那么重定向到以jobid命名的缓存中
                        name = LogUtil.cacheDir() + File.separator + Constants.PID + File.separator + jobId;
                        cacheLocation = name;

                        if (getRedirectJobCount() == 8) {
                            throw new IllegalStateException("The amount of async command that saving result to file can't > 8");
                        }
                    }
                    redirectHandler = new RedirectHandler(name, ">>".equals(tokenValue));
                    break;
                }
            }
            if (isPipeline) {
                pipelineTokens.add(remainingToken);
            } else {
                remaining.add(remainingToken);
            }
        }
        injectHandler(stdoutHandlerChain, pipelineTokens);
        if (redirectHandler != null) {
            stdoutHandlerChain.add(redirectHandler);
            term.write("redirect output file will be: " + redirectHandler.getFilePath()+"\n");
        } else {
            stdoutHandlerChain.add(new TermHandler(term));
            if (GlobalOptions.isSaveResult) {
                stdoutHandlerChain.add(new RedirectHandler());
            }
        }
        ProcessOutput processOutput = new ProcessOutput(stdoutHandlerChain, cacheLocation, term);
        ProcessImpl process = new ProcessImpl(command, remaining, command.processHandler(), processOutput, resultDistributor);
        process.setTty(term);
        return process;
    }

    /** 从重定向 token 之后读取目标文件名 */
    private String getRedirectFileName(ListIterator<CliToken> tokens) {
        while (tokens.hasNext()) {
            CliToken token = tokens.next();
            if (token.isText()) {
                return token.value();
            }
        }
        return null;
    }

    /** 将管道段 token 解析为 StdoutHandler 并加入输出链 */
    private void injectHandler(List<Function<String, String>> stdoutHandlerChain, List<CliToken> pipelineTokens) {
        if (!pipelineTokens.isEmpty()) {
            StdoutHandler handler = StdoutHandler.inject(pipelineTokens);
            if (handler != null) {
                stdoutHandlerChain.add(handler);
            }
            pipelineTokens.clear();
        }
    }

    @Override
    /** 无回调的 close，委托给 {@link #close(Handler)} */
    public void close() {
        close(null);
    }
}
