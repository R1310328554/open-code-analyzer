package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.RedefineModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code redefine} 热替换命令的终端渲染视图。
 * <p>
 * 类加载器未唯一匹配时先列出候选 ClassLoader 供用户二次指定；
 * 成功时输出替换类数量及全限定类名列表。
 *
 * @author gongdewei 2020/4/16
 */
public class RedefineView extends ResultView<RedefineModel> {

    @Override
    public void draw(CommandProcess process, RedefineModel result) {
        // 多 ClassLoader 歧义：引导用户用 -c / --classLoaderClass 指定
        if (result.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, result.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String aClass : result.getRedefinedClasses()) {
            sb.append(aClass).append("\n");
        }
        process.write("redefine success, size: " + result.getRedefinitionCount())
                .write(", classes:\n")
                .write(sb.toString());
    }

}
