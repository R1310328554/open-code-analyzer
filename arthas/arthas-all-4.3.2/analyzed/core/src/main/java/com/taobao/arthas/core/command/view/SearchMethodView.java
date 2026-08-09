package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.SearchMethodModel;
import com.taobao.arthas.core.command.model.MethodVO;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.ClassUtils;
import com.taobao.text.util.RenderUtil;


/**
 * {@code sm}（search method）命令的终端渲染视图。
 * <p>
 * ClassLoader 歧义时输出候选列表；详情模式渲染方法/构造器签名，
 * 否则输出 {@code 类名 方法名+描述符} 紧凑格式。
 *
 * @author gongdewei 2020/4/9
 */
public class SearchMethodView extends ResultView<SearchMethodModel> {
    @Override
    public void draw(CommandProcess process, SearchMethodModel result) {
        if (result.getMatchedClassLoaders() != null) {
            process.write("Matched classloaders: \n");
            ClassLoaderView.drawClassLoaders(process, result.getMatchedClassLoaders(), false);
            process.write("\n");
            return;
        }

        boolean detail = result.isDetail();
        MethodVO methodInfo = result.getMethodInfo();

        if (detail) {
            if (methodInfo.isConstructor()) {
                // 详情：渲染构造器声明（含修饰符、参数、异常）
                process.write(RenderUtil.render(ClassUtils.renderConstructor(methodInfo), process.width()) + "\n");
            } else {
                // 详情：渲染普通方法声明
                process.write(RenderUtil.render(ClassUtils.renderMethod(methodInfo), process.width()) + "\n");
            }
        } else {
            // 简洁模式示例：java.util.List indexOf(Ljava/lang/Object;)I
            process.write(methodInfo.getDeclaringClass())
                    .write(" ")
                    .write(methodInfo.getMethodName())
                    .write(methodInfo.getDescriptor())
                    .write("\n");
        }

    }
}
