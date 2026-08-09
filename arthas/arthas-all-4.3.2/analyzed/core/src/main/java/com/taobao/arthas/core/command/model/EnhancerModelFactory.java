package com.taobao.arthas.core.command.model;

import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.util.affect.EnhancerAffect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 将字节码增强副作用 {@link EnhancerAffect} 转换为可序列化 VO 的工厂。
 * <p>
 * 基础类型 {@link EnhancerModel}、{@link EnhancerAffectVO} 定义在 arthas-model 模块；
 * 本类根据 {@link GlobalOptions} 决定是否附带 dump 文件路径与 verbose 方法列表。
 *
 * Factory class for creating EnhancerModel and EnhancerAffectVO from EnhancerAffect.
 * The base EnhancerModel and EnhancerAffectVO are defined in arthas-model module.
 *
 * @author gongdewei 2020/7/20
 */
public class EnhancerModelFactory {

    /** 从 affect 构建增强结果，success 表示增强/卸载是否整体成功 */
    public static EnhancerModel create(EnhancerAffect affect, boolean success) {
        return new EnhancerModel(createEnhancerAffectVO(affect), success);
    }

    /** 附带用户可见 message（如部分类增强失败时的提示） */
    public static EnhancerModel create(EnhancerAffect affect, boolean success, String message) {
        return new EnhancerModel(createEnhancerAffectVO(affect), success, message);
    }

    /**
     * Create EnhancerAffectVO from EnhancerAffect.
     * This method is public so other classes like ResetModel can use it.
     * <p>
     * affect 为 null 时返回占位 VO（计数 -1），避免 NPE；ResetModel 等可复用本方法。
     */
    public static EnhancerAffectVO createEnhancerAffectVO(EnhancerAffect affect) {
        if (affect == null) {
            return new EnhancerAffectVO(-1, 0, 0, -1);
        }
        
        EnhancerAffectVO vo = new EnhancerAffectVO(
            affect.cost(),
            affect.mCnt(),
            affect.cCnt(),
            affect.getListenerId()
        );
        vo.setThrowable(affect.getThrowable());
        vo.setOverLimitMsg(affect.getOverLimitMsg());
        
        // 全局开启 dump 时，收集增强前备份的 class 文件绝对路径
        if (GlobalOptions.isDump) {
            List<String> classDumpFiles = new ArrayList<String>();
            for (File classDumpFile : affect.getClassDumpFiles()) {
                classDumpFiles.add(classDumpFile.getAbsolutePath());
            }
            vo.setClassDumpFiles(classDumpFiles);
        }

        // verbose 模式下附带被增强的方法签名列表，便于排查匹配范围
        if (GlobalOptions.verbose) {
            List<String> methods = new ArrayList<String>();
            methods.addAll(affect.getMethods());
            vo.setMethods(methods);
        }
        
        return vo;
    }
}
