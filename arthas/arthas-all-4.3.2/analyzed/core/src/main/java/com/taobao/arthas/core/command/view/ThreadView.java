package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.BusyThreadInfo;
import com.taobao.arthas.core.command.model.ThreadModel;
import com.taobao.arthas.core.command.model.ThreadVO;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.ThreadUtil;
import com.taobao.text.ui.LabelElement;
import com.taobao.text.util.RenderUtil;

import java.util.List;
import java.util.Map;


/**
 * {@code thread} 命令的终端渲染视图。
 * <p>
 * 按 {@link ThreadModel} 携带的数据分支：单线程堆栈、最忙线程列表、阻塞锁持有者、
 * 或全量线程统计表（含状态汇总行与 CPU 采样列）。
 *
 * @author gongdewei 2020/4/26
 */
public class ThreadView extends ResultView<ThreadModel> {

    @Override
    public void draw(CommandProcess process, ThreadModel result) {
        // 指定 -n 单线程：输出完整堆栈，不含 CPU 采样
        if (result.getThreadInfo() != null) {
            String content = ThreadUtil.getFullStacktrace(result.getThreadInfo());
            process.write(content);
        } else if (result.getBusyThreads() != null) {
            // -b 最忙线程模式：逐个输出堆栈
            List<BusyThreadInfo> threadInfos = result.getBusyThreads();
            for (BusyThreadInfo info : threadInfos) {
                String stacktrace = ThreadUtil.getFullStacktrace(info, -1, -1);
                process.write(stacktrace).write("\n");
            }
        } else if (result.getBlockingLockInfo() != null) {
            // 阻塞检测：展示持有锁的线程堆栈
            String stacktrace = ThreadUtil.getFullStacktrace(result.getBlockingLockInfo());
            process.write(stacktrace);

        } else if (result.getThreadStateCount() != null) {
            // 默认/全量列表：先汇总各状态计数，再渲染线程表格
            Map<Thread.State, Integer> threadStateCount = result.getThreadStateCount();
            List<ThreadVO> threadStats = result.getThreadStats();

            // 累加各 Thread.State 计数得到活跃线程总数
            int total = 0;
            for (Integer value : threadStateCount.values()) {
                total += value;
            }

            int internalThreadCount = 0;
            for (ThreadVO thread : threadStats) {
                // id<=0 为 JVM 内部线程，不计入 stateCount 需单独统计
                if (thread.getId() <= 0) {
                    internalThreadCount += 1;
                }
            }
            total += internalThreadCount;

            StringBuilder threadStat = new StringBuilder();
            threadStat.append("Threads Total: ").append(total);

            for (Thread.State s : Thread.State.values()) {
                Integer count = threadStateCount.get(s);
                threadStat.append(", ").append(s.name()).append(": ").append(count);
            }
            if (internalThreadCount > 0) {
                threadStat.append(", Internal threads: ").append(internalThreadCount);
            }
            String stat = RenderUtil.render(new LabelElement(threadStat), process.width());

            // 表格可见行数：-n 全量时等于列表大小，否则受终端高度限制
            int height;
            if (result.isAll()) {
                height = threadStats.size() + 1;
            } else {
                height = Math.max(5, process.height() - 2);
                // 避免表格底部出现过多空白行
                height = Math.min(height, threadStats.size() + 2);
            }
            String content = ViewRenderUtil.drawThreadInfo(threadStats, process.width(), height);
            process.write(stat + content);
        }
    }
}
