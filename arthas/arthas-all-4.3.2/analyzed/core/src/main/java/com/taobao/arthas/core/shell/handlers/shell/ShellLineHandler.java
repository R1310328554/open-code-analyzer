package com.taobao.arthas.core.shell.handlers.shell;

import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.arthas.core.shell.cli.CliTokens;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.impl.ShellImpl;
import com.taobao.arthas.core.shell.system.ExecStatus;
import com.taobao.arthas.core.shell.system.Job;
import com.taobao.arthas.core.shell.term.Term;
import com.taobao.arthas.core.util.TokenUtils;
import com.taobao.arthas.core.view.Ansi;

import java.util.List;

/**
 * Shell 主命令行处理器：解析用户输入并分派内置命令或 Arthas 诊断 Job。
 * <p>
 * 支持 exit/logout/q/quit、jobs、fg、bg、kill 等类 bash 内置命令，
 * 其余输入经 {@link CliTokens#tokenize} 解析后由 {@link ShellImpl#createJob} 创建并运行 Job。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class ShellLineHandler implements Handler<String> {

    /** 所属 Shell 会话 */
    private ShellImpl shell;
    /** 终端，用于输出与关闭 */
    private Term term;

    /** @param shell 处理输入行的 Shell 实例 */
    public ShellLineHandler(ShellImpl shell) {
        this.shell = shell;
        this.term = shell.term();
    }

    @Override
    /** 解析一行输入：内置命令、Job 管理或创建新诊断 Job */
    public void handle(String line) {
        if (line == null) {
            // EOF（Ctrl+D）：与 exit 相同流程
            handleExit();
            return;
        }

        List<CliToken> tokens = CliTokens.tokenize(line);
        CliToken first = TokenUtils.findFirstTextToken(tokens);
        if (first == null) {
            // 空行或仅含空白/管道符：重新显示提示符
            shell.readline();
            return;
        }

        String name = first.value();
        if (name.equals("exit") || name.equals("logout") || name.equals("q") || name.equals("quit")) {
            handleExit();
            return;
        } else if (name.equals("jobs")) {
            handleJobs();
            return;
        } else if (name.equals("fg")) {
            handleForeground(tokens);
            return;
        } else if (name.equals("bg")) {
            handleBackground(tokens);
            return;
        } else if (name.equals("kill")) {
            handleKill(tokens);
            return;
        }

        // 非内置命令：创建 Arthas 诊断 Job 并运行
        Job job = createJob(tokens);
        if (job != null) {
            job.run();
        }
    }

    /** 从参数字符串解析 Job ID，支持 %1 或 1 两种写法 */
    private int getJobId(String arg) {
        int result = -1;
        try {
            if (arg.startsWith("%")) {
                result = Integer.parseInt(arg.substring(1));
            } else {
                result = Integer.parseInt(arg);
            }
        } catch (Exception e) {
        }
        return result;
    }

    /** 调用 ShellImpl 创建 Job；解析失败时输出错误并重新 readline */
    private Job createJob(List<CliToken> tokens) {
        Job job;
        try {
            job = shell.createJob(tokens);
        } catch (Exception e) {
            term.echo(e.getMessage() + "\n");
            shell.readline();
            return null;
        }
        return job;
    }

    /** 处理 kill 命令：终止指定 Job */
    private void handleKill(List<CliToken> tokens) {
        String arg = TokenUtils.findSecondTokenText(tokens);
        if (arg == null) {
            term.write("kill: usage: kill job_id\n");
            shell.readline();
            return;
        }
        Job job = shell.jobController().getJob(getJobId(arg));
        if (job == null) {
            term.write(arg + " : no such job\n");
            shell.readline();
        } else {
            job.terminate();
            term.write("kill job " + job.id() + " success\n");
            shell.readline();
        }
    }

    /** 处理 bg 命令：将 STOPPED 状态的 Job 恢复为后台运行 */
    private void handleBackground(List<CliToken> tokens) {
        String arg = TokenUtils.findSecondTokenText(tokens);
        Job job;
        if (arg == null) {
            // 无参数时默认当前前台 Job
            job = shell.getForegroundJob();
        } else {
            job = shell.jobController().getJob(getJobId(arg));
        }
        if (job == null) {
            term.write(arg + " : no such job\n");
            shell.readline();
        } else {
            if (job.status() == ExecStatus.STOPPED) {
                job.resume(false);
                term.echo(shell.statusLine(job, ExecStatus.RUNNING));
                shell.readline();
            } else {
                term.write("job " + job.id() + " is already running\n");
                shell.readline();
            }
        }
    }

    /** 处理 fg 命令：将 Job 恢复到前台（resume 或 toForeground） */
    private void handleForeground(List<CliToken> tokens) {
        String arg = TokenUtils.findSecondTokenText(tokens);
        Job job;
        if (arg == null) {
            job = shell.getForegroundJob();
        } else {
            job = shell.jobController().getJob(getJobId(arg));
        }
        if (job == null) {
            term.write(arg + " : no such job\n");
            shell.readline();
        } else {
            if (job.getSession() != shell.session()) {
                // 不允许 fg 其他会话创建的 Job
                term.write("job " + job.id() + " doesn't belong to this session, so can not fg it\n");
                shell.readline();
            } else if (job.status() == ExecStatus.STOPPED) {
                job.resume(true);
            } else if (job.status() == ExecStatus.RUNNING) {
                // 已在运行：仅切到前台显示输出
                job.toForeground();
            } else {
                term.write("job " + job.id() + " is already terminated, so can not fg it\n");
                shell.readline();
            }
        }
    }

    /** 列出当前会话所有 Job 及其状态行 */
    private void handleJobs() {
        for (Job job : shell.jobController().jobs()) {
            String statusLine = shell.statusLine(job, job.status());
            term.write(statusLine);
        }
        shell.readline();
    }

    /** 退出当前 Shell 会话：提示 Arthas 仍在后台运行，需 stop 命令完全关闭 */
    private void handleExit() {
        String msg = Ansi.ansi().fg(Ansi.Color.GREEN).a("Session has been terminated.\n"
                + "Arthas is still running in the background.\n"
                + "To completely shutdown arthas, please execute the 'stop' command.\n").reset().toString();
        term.write(msg);
        term.close();
    }
}
