package com.taobao.arthas.core.shell.system;

/**
 * Job 执行状态枚举，描述任务在生命周期中的当前阶段。
 * <p>
 * 与 {@link Job} 的 run/suspend/terminate 操作配合，供 jobs 命令展示状态。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public enum ExecStatus {

    /** 已就绪，可转为 RUNNING 或直接 TERMINATED */
    READY,

    /** 正在运行，可 suspend 或 terminate */
    RUNNING,

    /** 已挂起（停止），可 resume 或 terminate */
    STOPPED,

    /** 已终止，不可再恢复 */
    TERMINATED


}
