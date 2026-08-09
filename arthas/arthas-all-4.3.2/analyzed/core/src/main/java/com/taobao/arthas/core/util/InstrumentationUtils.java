package com.taobao.arthas.core.util;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Collection;
import java.util.Set;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

/**
 * {@link Instrumentation} 字节码增强辅助：注册 Transformer 并对指定类批量 retransform。
 * <p>
 * 自动跳过 Lambda 合成类（JDK 不支持对其 retransform，见 arthas#1512）。
 *
 * @author hengyunabc 2020-05-25
 */
public class InstrumentationUtils {
    private static final Logger logger = LoggerFactory.getLogger(InstrumentationUtils.class);

    /**
     * 临时注册 {@code transformer} 并对 {@code classes} 中每个类执行 retransform，finally 中移除 Transformer。
     *
     * @param inst Instrumentation
     * @param transformer 可重入的 ClassFileTransformer
     * @param classes 待增强的已加载类集合
     */
    public static void retransformClasses(Instrumentation inst, ClassFileTransformer transformer,
            Set<Class<?>> classes) {
        try {
            inst.addTransformer(transformer, true);

            for (Class<?> clazz : classes) {
                if (ClassUtils.isLambdaClass(clazz)) {
                    logger.info(
                            "ignore lambda class: {}, because jdk do not support retransform lambda class: https://github.com/alibaba/arthas/issues/1512.",
                            clazz.getName());
                    continue;
                }
                try {
                    inst.retransformClasses(clazz);
                } catch (Throwable e) {
                    String errorMsg = "retransformClasses class error, name: " + clazz.getName();
                    logger.error(errorMsg, e);
                }
            }
        } finally {
            inst.removeTransformer(transformer);
        }
    }

    /**
     * 按类名集合触发已加载类的 retransform（不注册新 Transformer，依赖已有 agent 逻辑）。
     *
     * @param inst Instrumentation
     * @param classes 全限定类名集合
     */
    public static void trigerRetransformClasses(Instrumentation inst, Collection<String> classes) {
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (classes.contains(clazz.getName())) {
                try {
                    inst.retransformClasses(clazz);
                } catch (Throwable e) {
                    String errorMsg = "retransformClasses class error, name: " + clazz.getName();
                    logger.error(errorMsg, e);
                }
            }
        }
    }
}
