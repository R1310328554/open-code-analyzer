package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.command.model.OgnlModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.ObjectView;

/**
 * {@code ognl} 命令的终端渲染视图。
 * <p>
 * 输出 OGNL 表达式求值结果；ClassLoader 歧义时先列出候选。
 * 复杂对象经 {@link ObjectView} 展开后 toString。
 *
 * @author gongdewei 2020/4/29
 */
public class OgnlView extends ResultView<OgnlModel> {
    @Override
    public void draw(CommandProcess process, OgnlModel model) {
        // ClassLoader 歧义：列出候选后返回
        if (model.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, model.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }

        // needExpand 时用 ObjectView 递归展开对象图
        ObjectVO objectVO = model.getValue();
        String resultStr = StringUtils.objectToString(objectVO.needExpand() ? new ObjectView(objectVO).draw() : objectVO.getObject());
        process.write(resultStr).write("\n");
    }
}
