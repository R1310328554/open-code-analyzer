package com.taobao.arthas.core.shell.system;

/**
 * 可感知当前 {@link Process} 的增强监听器标记接口。
 * <p>
 * 部分 AdviceListener（如 watch/trace）在注册时需绑定所属 Process，
 * 以便在 unregister 时正确解除 AdviceWeaver 挂钩。
 *
 * @author hengyunabc 2020-05-18
 */
public interface ProcessAware {

    /** @return 当前绑定的 Shell 进程，未绑定时为 null */
    public Process getProcess();

    /** @param process 绑定 Shell 进程实例 */
    public void setProcess(Process process);

}
