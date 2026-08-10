package org.keycloak.testframework.util;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.keycloak.quarkus.runtime.Environment;

/**
 * Keycloak 测试进程生命周期辅助工具。
 * <p>
 * 解析启动脚本与实际 JVM 进程的 PID 差异，并在 Unix/Windows 上优雅或强制终止进程树。
 */
public class ProcessUtils {

    /**
     * 解析 Keycloak 实际运行中的 Java 进程 PID。
     * <p>
     * 重新增强（re-augmentation）时 {@code kc.sh} 通过 {@code exec} 替换自身，
     * 子进程列表为空，此时 shell 与 Java 共享同一 PID；否则 Java 为唯一子进程。
     *
     * @param keycloakProcess 启动 Keycloak 时返回的 {@link Process}
     * @return 应写入 PID 文件、用于后续清理的进程 ID
     * @throws RuntimeException 存在多个子进程时无法唯一确定 PID
     */
    public static long getKeycloakPid(Process keycloakProcess) {
        List<ProcessHandle> descendants = keycloakProcess.descendants().toList();
        if (descendants.isEmpty()) {
            // 重新增强时通过 exec 重启，Java 与 kc.sh 共用同一 PID
            return keycloakProcess.pid();
        } else if (descendants.size() == 1) {
            // 未重新增强时 Java 为 kc.sh 的唯一子进程，PID 不同于启动脚本
            return descendants.get(0).pid();
        } else {
            throw new RuntimeException("Started process has multiple descendants");
        }
    }

    /**
     * 在 Unix 系统上通过 {@code kill} 终止指定 PID（先 TERM 后 KILL）。
     *
     * @param pid 目标进程 ID 字符串
     * @return 命令成功退出时 {@code true}；Windows 或不支持时 {@code false}
     */
    public static boolean killProcess(String pid) {
        try {
            if (!Environment.isWindows()) {
                ProcessBuilder pb = new ProcessBuilder("kill", "--timeout", "10000", "TERM", "--timeout", "10000", "KILL", pid);
                Process process = pb.start();
                process.waitFor(10, TimeUnit.SECONDS);
                return process.exitValue() == 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /** 优雅终止进程及其子进程树（必要时递归强制终止）。 */
    public static void killRunningProcess(Process process) {
        killRunningProcess(process, false);
    }

    /**
     * 终止 Keycloak 进程树。
     * <p>
     * Windows 下先等待所有子进程退出；非强制模式失败时会递归调用自身并 {@code force=true}。
     *
     * @param process 待终止的根进程
     * @param force 是否使用 {@link Process#destroyForcibly()}
     */
    public static void killRunningProcess(Process process, boolean force) {
        try {
            if (Environment.isWindows()) {
                CompletableFuture<?> allProcesses = CompletableFuture.completedFuture(null);
                Iterator<ProcessHandle> itr = process.descendants().iterator();
                while (itr.hasNext()) {
                    ProcessHandle ph = itr.next();
                    if (force) {
                        ph.destroyForcibly();
                    } else {
                        ph.destroy();
                    }
                    allProcesses = CompletableFuture.allOf(allProcesses, ph.onExit());
                }
                allProcesses.get(10, TimeUnit.SECONDS);
            }

            if (force) {
                process.destroyForcibly();
            } else {
                process.destroy();
            }
            process.waitFor(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (!force) {
                killRunningProcess(process, true);
            } else {
                throw new RuntimeException("Failed to stop Keycloak process");
            }
        }
    }

}
