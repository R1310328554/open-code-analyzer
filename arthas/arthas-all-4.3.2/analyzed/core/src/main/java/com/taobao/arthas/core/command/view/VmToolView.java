package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.command.model.VmToolModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.ObjectView;

/**
 * {@code vmtool} 命令的终端渲染视图。
 * <p>
 * 支持 ClassLoader 匹配列表与 OGNL/表达式执行结果两种输出；
 * 复杂对象通过 {@link ObjectView} 展开。
 *
 * @author hengyunabc 2022-04-24
 */
public class VmToolView extends ResultView<VmToolModel> {
    @Override
    public void draw(CommandProcess process, VmToolModel model) {
        // ClassLoader 歧义：先列出匹配项再终止
        if (model.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, model.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }

        // 表达式执行结果：简单对象 toString，复杂对象树形展开
        ObjectVO objectVO = model.getValue();
        String resultStr = StringUtils.objectToString(objectVO.needExpand() ? new ObjectView(objectVO).draw() : objectVO.getObject());
        process.write(resultStr).write("\n");
    }
}
