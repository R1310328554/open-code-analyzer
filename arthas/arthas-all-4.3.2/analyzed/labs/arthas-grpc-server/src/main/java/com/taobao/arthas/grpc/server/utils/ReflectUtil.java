package com.taobao.arthas.grpc.server.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于类路径的简易反射扫描工具，用于启动时发现 gRPC 服务与执行器类。
 * <p>
 * 将包名转为资源路径，枚举目录下 {@code .class} 文件并通过 {@link Class#forName(String)} 加载。
 * 仅适用于开发/测试场景下的扁平包结构，不支持 jar 内嵌套包扫描。
 *
 * @author: FengYe
 * @date: 2024/9/6 02:20
 * @description: ReflectUtil
 */
public class ReflectUtil {
    /**
     * 扫描指定包名下所有顶层 class 文件并加载为 {@link Class} 对象。
     *
     * @param packageName 点分隔的包名，如 {@code com.taobao.arthas.grpc.server.handler.executor}
     * @return 成功加载的类列表；扫描失败或目录不存在时返回空列表
     */
    public static List<Class<?>> findClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            if (resource != null) {
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    for (File file : directory.listFiles()) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = packageName + '.' + file.getName().replace(".class", "");
                            classes.add(Class.forName(className));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 扫描失败时静默返回已收集结果，由调用方处理空列表
        }
        return classes;
    }
}
