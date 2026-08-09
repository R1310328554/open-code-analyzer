package com.taobao.arthas.core.shell;

import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.system.JobController;

import java.util.List;

/**
 * 用户与 Arthas Shell 之间的交互会话抽象。
 * <p>
 * 负责创建 {@link Job}、暴露 {@link JobController} 与 {@link Session}，
 * 并在连接关闭时释放资源。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface Shell {

    /**
     * 根据已解析的命令行 Token 创建 Job，需再调用 {@link Job#run()} 执行。
     *
     * @param line the command line creating this job
     * @return the created job
     */
    Job createJob(List<CliToken> line);

    /** 将原始命令字符串解析为 Token 后创建 Job，参见 {@link #createJob(List)} */

    Job createJob(String line);

    /** @return 当前 Shell 关联的作业控制器（前台/后台任务） */

    JobController jobController();

    /** @return 当前 Shell 会话（含终端、认证 Subject 等） */

    Session session();

    /** 关闭 Shell 并附带关闭原因（如 EOF、超时） */

    void close(String reason);
}

