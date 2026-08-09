package com.taobao.arthas.core.command.express;

import java.lang.ref.WeakReference;

/**
 * {@link Express} 实例工厂：提供线程本地复用与非池化两种创建方式。
 *
 * @author ralf0131 2017-01-04 14:40.
 * @author hengyunabc 2018-10-08
 */
public class ExpressFactory {

    /**
     * 这里不能直接在 ThreadLocalMap 里强引用 Express（它由 ArthasClassLoader 加载），否则 stop/detach 后会被业务线程持有，
     * 导致 ArthasClassLoader 无法被 GC 回收。
     *
     * 用 WeakReference 打断强引用链：Thread -> ThreadLocalMap -> value(WeakReference) -X-> Express。
     */
    private static final ThreadLocal<WeakReference<Express>> expressRef = ThreadLocal
            .withInitial(() -> new WeakReference<Express>(new OgnlExpress()));

    /**
     * 获取当前线程复用的 Express，绑定目标对象并重置上下文后返回。
     *
     * @param object 作为 OGNL 根对象的绑定目标
     * @return 已 reset 并 bind 的 Express 实例
     */
    public static Express threadLocalExpress(Object object) {
        WeakReference<Express> reference = expressRef.get();
        Express express = reference == null ? null : reference.get();
        if (express == null) {
            express = new OgnlExpress();
            expressRef.set(new WeakReference<Express>(express));
        }
        return express.reset().bind(object);
    }

    /**
     * 创建不进入 ThreadLocal 的 Express，使用指定 ClassLoader 解析类名。
     *
     * @param classloader 目标类加载器，null 时使用系统 ClassLoader
     * @return 新的 {@link OgnlExpress} 实例
     */
    public static Express unpooledExpress(ClassLoader classloader) {
        if (classloader == null) {
            classloader = ClassLoader.getSystemClassLoader();
        }
        return new OgnlExpress(new ClassLoaderClassResolver(classloader));
    }
}
