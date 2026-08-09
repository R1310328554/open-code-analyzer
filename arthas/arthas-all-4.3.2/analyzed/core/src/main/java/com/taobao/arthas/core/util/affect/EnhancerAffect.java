package com.taobao.arthas.core.util.affect;

import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.util.ClassLoaderUtils;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

/**
 * 字节码增强（watch/trace/monitor 等）的影响范围统计。
 * <p>
 * 线程安全地累计受影响类数、方法数，可选记录 dump 文件、
 * 监听器 id 与增强过程中的异常。
 * </p>
 * Created by vlinux on 15/5/19.
 * @author hengyunabc 2020-06-01
 */
public final class EnhancerAffect extends Affect {

    /** 受影响类计数。 */
    private final AtomicInteger cCnt = new AtomicInteger();
    /** 受影响方法计数。 */
    private final AtomicInteger mCnt = new AtomicInteger();
    /** 关联的 ClassFileTransformer，便于卸载。 */
    private ClassFileTransformer transformer;
    /** Advice 监听器 id。 */
    private long listenerId;

    /** 增强过程捕获的异常。 */
    private Throwable throwable;

    /**
     * dumpClass的文件存放集合
     */
    private final Collection<File> classDumpFiles = new ArrayList<File>();

    private final List<String> methods = new ArrayList<String>();

    private String overLimitMsg;

    public EnhancerAffect() {
    }

    /**
     * 影响类统计
     *
     * @param cc 类影响计数
     * @return 当前影响类个数
     */
    public int cCnt(int cc) {
        return cCnt.addAndGet(cc);
    }

    /**
     * 影响方法统计
     *
     * @param mc 方法影响计数
     * @return 当前影响方法个数
     */
    public int mCnt(int mc) {
        return mCnt.addAndGet(mc);
    }

    /**
     * 记录一条受影响方法并递增方法计数。
     *
     * @param classLoader 目标类 ClassLoader
     * @param clazz 内部名或类名
     * @param method 方法名
     * @param methodDesc 方法描述符
     * @return 更新后的方法总数
     */
    public int addMethodAndCount(ClassLoader classLoader, String clazz, String method, String methodDesc) {
        this.methods.add(ClassLoaderUtils.classLoaderHash(classLoader) + "|" + clazz.replace('/', '.') + "#" + method + "|" + methodDesc);
        return mCnt.addAndGet(1);
    }

    /**
     * 获取影响类个数
     *
     * @return 影响类个数
     */
    public int cCnt() {
        return cCnt.get();
    }

    /**
     * 获取影响方法个数
     *
     * @return 影响方法个数
     */
    public int mCnt() {
        return mCnt.get();
    }

    /** 记录 retransform 时 dump 的 class 文件路径。 */
    public void addClassDumpFile(File file) {
        classDumpFiles.add(file);
    }

    /** 返回关联 transformer。 */
    public ClassFileTransformer getTransformer() {
        return transformer;
    }

    /** 绑定 transformer。 */
    public void setTransformer(ClassFileTransformer transformer) {
        this.transformer = transformer;
    }

    /** 监听器 id。 */
    public long getListenerId() {
        return listenerId;
    }

    /** 设置监听器 id。 */
    public void setListenerId(long listenerId) {
        this.listenerId = listenerId;
    }

    /** 增强异常。 */
    public Throwable getThrowable() {
        return throwable;
    }

    /** 记录增强异常。 */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /** dump 文件集合。 */
    public Collection<File> getClassDumpFiles() {
        return classDumpFiles;
    }

    /** 受影响方法的编码列表（loader|类#方法|描述符）。 */
    public List<String> getMethods() {
        return methods;
    }

    /** 超过增强数量上限时的提示信息。 */
    public String getOverLimitMsg() {
        return overLimitMsg;
    }

    /** 设置超限提示。 */
    public void setOverLimitMsg(String overLimitMsg) {
        this.overLimitMsg = overLimitMsg;
    }

    @Override
    public String toString() {
        // TODO: 逐步移除 toString，改由 ViewRenderUtil.renderEnhancerAffect() 渲染        //TODO removing EnhancerAffect.toString(), replace with ViewRenderUtil.renderEnhancerAffect()
        final StringBuilder infoSB = new StringBuilder();
        if (GlobalOptions.isDump
                && !classDumpFiles.isEmpty()) {

            for (File classDumpFile : classDumpFiles) {
                infoSB.append("[dump: ").append(classDumpFile.getAbsoluteFile()).append("]\n");
            }
        }

        if (GlobalOptions.verbose && !methods.isEmpty()) {
            for (String method : methods) {
                infoSB.append("[Affect method: ").append(method).append("]\n");
            }
        }
        infoSB.append(format("Affect(class count: %d , method count: %d) cost in %s ms, listenerId: %d",
                cCnt(),
                mCnt(),
                cost(),
                listenerId));
        if (this.throwable != null) {
            infoSB.append("\nEnhance error! exception: ").append(this.throwable);
        }
        return infoSB.toString();
    }

}
