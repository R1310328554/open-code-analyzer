package org.keycloak.testframework;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * JUL {@link Filter}：拦截日志记录到内部队列而不输出，测试失败时可选择转发或丢弃。
 */
public class LogFilter implements Filter {

    private final Queue<LogRecord> queue = new LinkedList<>();

    /** 缓存记录并阻止 Handler 立即输出。 */
    @Override
    public boolean isLoggable(LogRecord record) {
        queue.add(record);
        return false;
    }

    /**
     * 清空队列。
     * @param forwardLogs 为 true 时将缓冲日志写回对应 Logger
     */
    public void clear(boolean forwardLogs) {
        if (forwardLogs) {
            for (LogRecord r = queue.poll(); r != null; r = queue.poll()) {
                Logger.getLogger(r.getLoggerName()).log(r.getLevel(), r.getMessage(), r.getParameters());
            }
        } else {
            queue.clear();
        }
    }

}
