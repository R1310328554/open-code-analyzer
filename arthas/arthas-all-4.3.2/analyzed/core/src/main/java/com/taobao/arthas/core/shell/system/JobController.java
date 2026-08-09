package com.taobao.arthas.core.shell.system;

import com.taobao.arthas.core.distribution.ResultDistributor;
import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.system.impl.InternalCommandManager;
import com.taobao.arthas.core.shell.term.Term;

import java.util.List;
import java.util.Set;

/**
 * Job 控制器接口，管理 Shell 会话中的任务创建、查询与关闭。
 * <p>
 * 每个 Shell 或全局实例维护活跃 Job 集合；关闭时终止全部底层 Process。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface JobController {

    /** @return 当前控制器下所有活跃 Job */
    Set<Job> jobs();

    /**
     * 按数字 id 查找活跃 Job。
     *
     * @param id the job id
     * @return the job of {@literal null} when not found
     */
    Job getJob(int id);

    /**
     * 根据 CLI Token 创建并注册 Job。
     *
     * @param commandManager command manager
     * @param tokens    the command tokens
     * @param session     the current session
     * @param jobHandler  job event handler
     * @param term     telnet term
     * @param resultDistributor
     * @return the created job
     */
    Job createJob(InternalCommandManager commandManager, List<CliToken> tokens, Session session, JobListener jobHandler, Term term, ResultDistributor resultDistributor);

    /** 关闭控制器并终止全部 Job；关闭后不可再创建任务 */
    void close(Handler<Void> completionHandler);

    /** 关闭 Shell 会话并终止全部 Job（无 completion 回调版本） */
    void close();

}
