package com.taobao.arthas.core.shell.handlers.term;

import com.taobao.arthas.core.shell.term.impl.TermImpl;
import io.termd.core.function.BiConsumer;
import io.termd.core.tty.TtyEvent;

/**
 * TTY 控制事件 Handler：将 termd {@link TtyEvent} 分派到 {@link TermImpl}。
 * <p>
 * 处理中断（INTR）、文件结束（EOF）、挂起（SUSP）三类终端信号，
 * 分别触发 Shell 层的中断、退出与 Job 暂停逻辑。
 *
 * @author beiwei30 on 23/11/2016.
 */
public class EventHandler implements BiConsumer<TtyEvent, Integer> {
    /** 接收 TTY 事件并分派的 Term 实例 */
    private TermImpl term;

    /** @param term 关联的 Term 实现 */
    public EventHandler(TermImpl term) {
        this.term = term;
    }

    @Override
    /** 按事件类型调用 TermImpl 对应的 handle 方法 */
    public void accept(TtyEvent event, Integer key) {
        switch (event) {
            case INTR:
                // Ctrl+C：中断前台命令或结束 readline
                term.handleIntr(key);
                break;
            case EOF:
                // Ctrl+D：EOF，触发 exit 流程
                term.handleEof(key);
                break;
            case SUSP:
                // Ctrl+Z：挂起前台 Job
                term.handleSusp(key);
                break;
        }
    }
}
