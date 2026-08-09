package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.FieldVO;
import com.taobao.arthas.core.command.model.SearchClassModel;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.ClassUtils;
import com.taobao.text.util.RenderUtil;

/**
 * {@code sc}（search class）命令的终端渲染视图。
 * <p>
 * 支持三种输出：ClassLoader 歧义列表、详细类信息表（含可选字段）、
 * 或简洁的类全限定名逐行列表。
 *
 * @author gongdewei 2020/4/8
 */
public class SearchClassView extends ResultView<SearchClassModel> {
    @Override
    public void draw(CommandProcess process, SearchClassModel result) {
        // 多 ClassLoader 匹配：先让用户选定加载器
        if (result.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, result.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }

        if (result.isDetailed()) {
            // -d 详情模式：渲染类结构、可选字段信息
            process.write(RenderUtil.render(ClassUtils.renderClassInfo(result.getClassInfo(),
                    result.isWithField()), process.width()));
            process.write("\n");
        } else if (result.getClassNames() != null) {
            // 默认：每行一个类名
            for (String className : result.getClassNames()) {
                process.write(className).write("\n");
            }
        }
    }

}
