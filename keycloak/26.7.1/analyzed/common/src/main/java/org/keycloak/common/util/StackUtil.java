package org.keycloak.common.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

/**
 * 在 TRACE 级别下输出精简堆栈跟踪的工具。
 *
 * @author hmlnarik
 */
public class StackUtil {

    private static final Logger LOG = Logger.getLogger("org.keycloak.STACK_TRACE");

    /** 按前缀缓存的堆栈字符串对象，避免重复构建。 */
    private static final ConcurrentHashMap<String, Object> STACK_TRACE_OBJECTS = new ConcurrentHashMap<>();

    /**
     * 返回当前调用栈的字符串表示（不含本方法帧），并过滤 {@code sun.}、
     * {@code java.lang.reflect} 等通常无关帧；遇到首个 {@code org.jboss.resteasy}
     * 帧即停止，通常止于处理 REST 端点的方法。
     *
     * <p>每行前缀为 {@code "\n    "}。</p>
     *
     * @return If the logger {@code org.keycloak.STACK_TRACE} is set to trace
     * level, then returns stack trace, else returns empty {@link StringBuilder}
     */
    public static Object getShortStackTrace() {
        return getShortStackTrace("\n    ");
    }

    /** 匹配应忽略的堆栈帧类名前缀/包名。 */
    private static final Pattern IGNORED = Pattern.compile("sun\\.|"
      + "java\\.(lang|util|stream)\\.|"
      + "jdk\\.internal\\.|"
      + "org\\.jboss\\.(arquillian|logging|logmanager|threads).|"
      + "org.apache.maven.surefire|"
      + "org\\.xnio\\.|"
      + "org\\.junit\\.|"
      + "org\\.infinispan\\.(interceptors|cache|notifications\\.cachelistener)\\.|"
      + "io\\.quarkus\\.|"
      + "io\\.undertow\\.|"
      + "picocli\\.|"
      + "org.keycloak.testsuite.model.KeycloakModelTest\\."
    );
    private static final StringBuilder EMPTY = new StringBuilder(0);

    /**
     * 返回当前调用栈的字符串表示（不含本方法帧），过滤规则同 {@link #getShortStackTrace()}。
     *
     * @param prefix Prefix to prepend to every stack trace line
     * @return If the logger {@code org.keycloak.STACK_TRACE} is set to trace
     * level, then returns stack trace, else returns empty {@link StringBuilder}
     */
    public static Object getShortStackTrace(final String prefix) {
        if (! isShortStackTraceEnabled()) return EMPTY;

        Object res = STACK_TRACE_OBJECTS.get(prefix);
        if (res == null) {
            res = stackTraceObject(prefix);
            // 不同步：缓存可被覆盖，最终内容相同
            STACK_TRACE_OBJECTS.put(prefix, res);
        }
        return res;
    }

    /** 懒构建堆栈字符串的匿名对象，在 {@code toString()} 时采集栈帧。 */
    private static Object stackTraceObject(final String prefix) {
        return new Object() {
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
                boolean stackTraceStarted = false;
                for (int endIndex = 0; endIndex < stackTrace.length; endIndex++) {
                    StackTraceElement st = stackTrace[endIndex];
                    if (! stackTraceStarted) {
                        stackTraceStarted = (getClass().getName().equals(st.getClassName()));
                        endIndex++;
                        continue;
                    }
                    if (IGNORED.matcher(st.getClassName()).find()) {
                        continue;
                    }
                    if (st.getClassName().startsWith("org.jboss.resteasy")) {
                        break;
                    }
                    sb.append(prefix).append(st);
                }
                return sb.toString();
            }
        };
    }

    /** 是否启用了精简堆栈跟踪（logger 为 TRACE）。 */
    public static boolean isShortStackTraceEnabled() {
        return LOG.isTraceEnabled();
    }
}
