package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.MemoryCompilerModel;
import com.taobao.arthas.core.shell.command.CommandProcess;

/**
 * {@code mc}（Memory Compiler）命令的终端渲染视图。
 * <p>
 * 列出内存编译生成的 .class 文件路径；ClassLoader 歧义时先输出候选列表。
 *
 * @author gongdewei 2020/4/20
 */
public class MemoryCompilerView extends ResultView<MemoryCompilerModel> {
    @Override
    public void draw(CommandProcess process, MemoryCompilerModel result) {
        // ClassLoader 歧义：列出候选后返回
        if (result.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, result.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }
        // 逐行输出编译产物路径
        process.write("Memory compiler output:\n");
        for (String file : result.getFiles()) {
            process.write(file + '\n');
        }
    }
}
