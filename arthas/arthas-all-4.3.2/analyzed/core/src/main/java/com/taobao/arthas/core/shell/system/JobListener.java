package com.taobao.arthas.core.shell.system;

/**
 * Job 生命周期事件监听器，供 Shell 响应前台/后台切换与终止。
 * <p>
 * {@link ShellImpl.ShellJobHandler} 实现本接口以恢复 readline 并保存命令历史。
 *
 * @author gongdewei 2020-03-23
 */
public interface JobListener {

    /** Job 切换到前台时回调（更新 Shell 前台 Job 引用） */
    void onForeground(Job job);

    /** Job 送入后台时回调（通常恢复 readline） */
    void onBackground(Job job);

    /** Job 终止时回调（保存历史并恢复 readline） */
    void onTerminated(Job job);

    /** Job 挂起时回调（非后台 Job 则恢复 readline） */
    void onSuspend(Job job);
}
