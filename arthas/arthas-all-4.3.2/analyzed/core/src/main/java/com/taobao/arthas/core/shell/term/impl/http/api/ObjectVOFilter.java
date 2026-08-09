package com.taobao.arthas.core.shell.term.impl.http.api;

import com.alibaba.fastjson2.filter.ValueFilter;
import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.ObjectView;

/**
 * Fastjson2 序列化过滤器：将 {@link ObjectVO} 转为可读字符串再输出 JSON。
 * <p>
 * 供 {@link HttpApiHandler} 在 {@code JSON.toJSONBytes} 时使用，
 * 避免直接序列化复杂对象图。
 *
 * @author hengyunabc 2022-08-24
 *
 */
public class ObjectVOFilter implements ValueFilter {

    @Override
    public Object apply(Object object, String name, Object value) {
        if (value instanceof ObjectVO) {
            ObjectVO vo = (ObjectVO) value;
            // needExpand 时用 ObjectView 展开绘制，否则直接 toString
            String resultStr = StringUtils.objectToString(vo.needExpand() ? new ObjectView(vo).draw() : value);
            return resultStr;
        }
        return value;
    }

}
