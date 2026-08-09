package java.arthas;

/**
 * Arthas 字节码增强插桩的统一回调入口。
 * <p>
 * 被增强的业务代码通过本类静态方法触发 spy 回调；实际逻辑由可替换的
 * {@link AbstractSpy} 实现承载。未 attach 或已 destroy 时使用 {@link NopSpy} 空实现，
 * 避免对业务路径产生开销。
 * </p>
 *
 * <pre>
 * 一个adviceId 是什么呢？ 就是一个trace/monitor/watch命令能对应上的一个id，比如一个类某个函数，它的 enter/end/exception 统一是一个id，分配完了就不会再分配。
 * 
 * 同样一个method，如果它trace之后，也会有一个 adviceId， 这个method里的所有invoke都是统一处理，认为是一个 adviceId 。 但如果有匹配到不同的 invoke的怎么分配？？
 * 好像有点难了。。
 * 
 * 其实就是把所有可以插入的地方都分类好，那么怎么分类呢？？ 或者是叫同一种匹配，就是同一种的 adviceId? 
 * 
 * 比如入参是有  class , method ,是固定的  ,  某个行号，或者 某个
 * 
 * aop插入的叫 adviceId ， command插入的叫 ListenerId？
 * 
 * 
 * 
 * </pre>
 * 
 * @author hengyunabc
 *
 */
public class SpyAPI {
    /** 空实现 spy，默认占位，不产生任何监控开销 */
    public static final AbstractSpy NOPSPY = new NopSpy();
    /** 当前生效的 spy 实例，volatile 保证多线程可见性 */
    private static volatile AbstractSpy spyInstance = NOPSPY;

    /** 是否已完成 init，表示 agent 已就绪 */
    public static volatile boolean INITED;

    /** 返回当前 spy 实现 */
    public static AbstractSpy getSpy() {
        return spyInstance;
    }

    /** 替换 spy 实现（attach 时注入真实逻辑） */
    public static void setSpy(AbstractSpy spy) {
        spyInstance = spy;
    }

    /** 恢复为空 spy，detach 或 destroy 时调用 */
    public static void setNopSpy() {
        setSpy(NOPSPY);
    }

    /** 当前是否为无操作的空 spy */
    public static boolean isNopSpy() {
        return NOPSPY == spyInstance;
    }

    /** 标记 agent 初始化完成 */
    public static void init() {
        INITED = true;
    }

    public static boolean isInited() {
        return INITED;
    }

    /** 销毁 spy：恢复空实现并清除初始化标记 */
    public static void destroy() {
        setNopSpy();
        INITED = false;
    }

    /** 方法入口回调，由增强字节码在方法进入时调用 */
    public static void atEnter(Class<?> clazz, String methodInfo, Object target, Object[] args) {
        spyInstance.atEnter(clazz, methodInfo, target, args);
    }

    /** 方法正常返回回调 */
    public static void atExit(Class<?> clazz, String methodInfo, Object target, Object[] args,
            Object returnObject) {
        spyInstance.atExit(clazz, methodInfo, target, args, returnObject);
    }

    /** 方法异常退出回调 */
    public static void atExceptionExit(Class<?> clazz, String methodInfo, Object target,
            Object[] args, Throwable throwable) {
        spyInstance.atExceptionExit(clazz, methodInfo, target, args, throwable);
    }

    /** 方法体内 invoke 调用前回调 */
    public static void atBeforeInvoke(Class<?> clazz, String invokeInfo, Object target) {
        spyInstance.atBeforeInvoke(clazz, invokeInfo, target);
    }

    /** 方法体内 invoke 调用后回调 */
    public static void atAfterInvoke(Class<?> clazz, String invokeInfo, Object target) {
        spyInstance.atAfterInvoke(clazz, invokeInfo, target);
    }

    /** 方法体内 invoke 抛出异常时的回调 */
    public static void atInvokeException(Class<?> clazz, String invokeInfo, Object target, Throwable throwable) {
        spyInstance.atInvokeException(clazz, invokeInfo, target, throwable);
    }

    /** 行级 trace 回调，携带局部变量与参数名等调试信息 */
    public static void atLine(Class<?> clazz, String methodInfo, int lineNumber, Object target, Object[] args,
            String[] argNames, Object[] localVars, String[] localVarNames) {
        spyInstance.atLine(clazz, methodInfo, lineNumber, target, args, argNames, localVars, localVarNames);
    }

    /** spy 回调接口，由 agent 侧实现具体的 trace/watch/monitor 逻辑 */
    public static abstract class AbstractSpy {
        public abstract void atEnter(Class<?> clazz, String methodInfo, Object target,
                Object[] args);

        public abstract void atExit(Class<?> clazz, String methodInfo, Object target, Object[] args,
                Object returnObject);

        public abstract void atExceptionExit(Class<?> clazz, String methodInfo, Object target,
                Object[] args, Throwable throwable);

        public abstract void atBeforeInvoke(Class<?> clazz, String invokeInfo, Object target);

        public abstract void atAfterInvoke(Class<?> clazz, String invokeInfo, Object target);

        public abstract void atInvokeException(Class<?> clazz, String invokeInfo, Object target, Throwable throwable);

        public abstract void atLine(Class<?> clazz, String methodInfo, int lineNumber, Object target, Object[] args,
                String[] argNames, Object[] localVars, String[] localVarNames);
    }

    /** 空 spy：所有回调均为 no-op，用于未 attach 或已 detach 状态 */
    static class NopSpy extends AbstractSpy {

        @Override
        public void atEnter(Class<?> clazz, String methodInfo, Object target, Object[] args) {
        }

        @Override
        public void atExit(Class<?> clazz, String methodInfo, Object target, Object[] args,
                Object returnObject) {
        }

        @Override
        public void atExceptionExit(Class<?> clazz, String methodInfo, Object target, Object[] args,
                Throwable throwable) {
        }

        @Override
        public void atBeforeInvoke(Class<?> clazz, String invokeInfo, Object target) {

        }

        @Override
        public void atAfterInvoke(Class<?> clazz, String invokeInfo, Object target) {

        }

        @Override
        public void atInvokeException(Class<?> clazz, String invokeInfo, Object target, Throwable throwable) {

        }

        @Override
        public void atLine(Class<?> clazz, String methodInfo, int lineNumber, Object target, Object[] args,
                String[] argNames, Object[] localVars, String[] localVarNames) {

        }

    }
}
