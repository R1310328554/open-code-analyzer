package com.taobao.arthas.core.view;

/**
 * 命令行文本视图统一接口。
 * <p>
 * 各具体控件（表格、树、对象等）实现 {@link #draw()} 将结构化数据
 * 渲染为终端可读的 ASCII/ANSI 字符串。
 * Created by vlinux on 15/5/7.
 */
public interface View {

    /**
     * 将视图内容渲染为最终输出字符串。
     *
     * @return 可直接打印到终端的多行文本
     */
    String draw();

}