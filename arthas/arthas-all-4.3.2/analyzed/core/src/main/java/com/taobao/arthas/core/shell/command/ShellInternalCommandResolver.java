package com.taobao.arthas.core.shell.command;

/**
 * Shell 内部命令的 {@link CommandResolver} 标记接口。
 * <p>
 * 实现该接口的 resolver 只注册 shell 自身控制命令（如 {@code help}、{@code exit}），
 * 不参与普通诊断命令查找，避免被 telnet/http/mcp 等远程执行链路误命中。
 * <p>
 * 与普通 {@link CommandRegistry} 分离，保证内部控制面与业务命令命名空间隔离。
 */
public interface ShellInternalCommandResolver extends CommandResolver {
}
