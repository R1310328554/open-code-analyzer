package com.taobao.arthas.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 命令结果分页与批量处理工具。
 * <p>
 * 将 {@link Class} 集合按固定页大小切分为类名字符串列表，
 * 通过 {@link PaginationHandler} 回调逐段输出，避免一次性返回过多类名。
 *
 * @author gongdewei 2020/5/18
 */
public class ResultUtils {

    /**
     * 将类集合按页大小切分，每页转换为全限定类名列表后交给回调处理。
     *
     * @param classes 待处理的已加载类集合
     * @param pageSize 每页最大类名数量
     * @param handler 分页回调，返回 false 时提前终止
     */
    public static void processClassNames(Collection<Class<?>> classes, int pageSize, PaginationHandler<List<String>> handler) {
        List<String> classNames = new ArrayList<String>(pageSize);
        int segment = 0;
        for (Class aClass : classes) {
            classNames.add(aClass.getName());
            // 达到页大小，提交当前分段
            if(classNames.size() >= pageSize) {
                if (!handler.handle(classNames, segment++)) {
                    return;
                }
                classNames = new ArrayList<String>(pageSize);
            }
        }
        // 处理最后不足一页的剩余分段
        if (classNames.size() > 0) {
            handler.handle(classNames, segment++);
        }
    }

    /**
     * 分页数据处理回调接口。
     *
     * @param <T> 单页数据类型
     */
    public interface PaginationHandler<T> {

        /**
         * 处理单页数据。
         *
         * @param list 当前页的类名或其它数据
         * @param segment 分段序号，从 0 递增
         * @return true 继续处理后续分段；false 终止分页
         */
        boolean handle(T list, int segment);
    }
}
