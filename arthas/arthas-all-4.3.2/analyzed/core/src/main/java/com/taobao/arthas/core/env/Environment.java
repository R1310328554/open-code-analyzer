package com.taobao.arthas.core.env;

/**
 * 配置环境顶层接口，继承 {@link PropertyResolver} 以提供属性解析能力。
 * <p>
 * Arthas 启动时通过 {@link StandardEnvironment} 等实现聚合系统属性、环境变量、
 * 命令行参数等多路 {@link PropertySource}，供 {@link com.taobao.arthas.core.config.BinderUtils} 绑定配置。
 */
public interface Environment extends PropertyResolver {

}
