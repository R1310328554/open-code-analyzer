package com.taobao.arthas.core.server.instrument;

import com.alibaba.bytekit.agent.inst.Instrument;
import com.alibaba.bytekit.agent.inst.InstrumentApi;

/**
 * 对 {@link java.lang.ClassLoader#loadClass(String)} 的字节码增强桩类。
 * <p>
 * 当类名以 {@code java.arthas.} 开头时，委托扩展类加载器加载，
 * 否则调用原始 loadClass 逻辑；由 {@link ArthasBootstrap#enhanceClassLoader()} 注入。
 *
 * @see java.lang.ClassLoader#loadClass(String)
 * @author hengyunabc 2020-11-30
 */
@Instrument(Class = "java.lang.ClassLoader")
public abstract class ClassLoader_Instrument {
    /** 增强后的 loadClass：Arthas 内部类走 ExtClassLoader */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.startsWith("java.arthas.")) {
            ClassLoader extClassLoader = ClassLoader.getSystemClassLoader().getParent();
            if (extClassLoader != null) {
                return extClassLoader.loadClass(name);
            }
        }

        // 非 arthas 包名：执行被增强前的原始 loadClass
        Class clazz = InstrumentApi.invokeOrigin();
        return clazz;
    }
}
