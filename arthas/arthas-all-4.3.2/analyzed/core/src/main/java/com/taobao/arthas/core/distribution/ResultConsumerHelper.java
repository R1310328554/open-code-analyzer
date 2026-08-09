package com.taobao.arthas.core.distribution;

import com.alibaba.fastjson2.JSON;
import com.taobao.arthas.core.command.model.Countable;
import com.taobao.arthas.core.command.model.ResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 命令结果模型的辅助工具类。
 * <p>
 * 提供 {@link #getItemCount(ResultModel)} 用于估算单条结果包含的数据元素数量，
 * 供 {@link impl.ResultConsumerImpl} 在批量拉取时决定是否立即 flush，避免单次推送过大。
 *
 * @author gongdewei 2020/5/18
 */
public class ResultConsumerHelper {

    private static final Logger logger = LoggerFactory.getLogger(ResultConsumerHelper.class);

    /** 按模型类名缓存可统计元素数量的反射字段，避免频繁反射产生内存碎片 */
    private static ConcurrentHashMap<String, List<Field>> modelFieldMap = new ConcurrentHashMap<String, List<Field>>();

    /**
     * 估算命令执行结果中包含的数据元素数量。
     * <p>
     * 作为 Consumer 分发时进行切片的参考依据，避免单次发送大量数据。
     * 注意：此方法调用频繁，实现上缓存 Field 对象以减少反射开销。
     *
     * @param model 待估算的结果模型
     * @return 元素数量，至少为 1
     */
    public static int getItemCount(ResultModel model) {
        // 实现 Countable 接口的模型自行统计元素数量
        if (model instanceof Countable) {
            return ((Countable) model).size();
        }

        // 普通 Model：通过反射统计 Collection/Map/Array/Countable 类型字段的元素数
        // 缓存 Field 对象，避免重复扫描类结构
        Class modelClass = model.getClass();
        List<Field> fields = modelFieldMap.get(modelClass.getName());
        if (fields == null) {
            fields = new ArrayList<Field>();
            Field[] declaredFields = modelClass.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                Class<?> fieldClass = field.getType();
                // 仅缓存可统计元素数量的容器类字段
                if (Collection.class.isAssignableFrom(fieldClass)
                        || Map.class.isAssignableFrom(fieldClass)
                        || Countable.class.isAssignableFrom(fieldClass)
                        || fieldClass.isArray()) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            List<Field> old_fields = modelFieldMap.putIfAbsent(modelClass.getName(), fields);
            if (old_fields != null) {
                fields = old_fields;
            }
        }

        // 遍历缓存字段累加元素数量
        int count = 0;
        try {
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Object value = field.get(model);
                if (value != null) {
                    if (value instanceof Collection) {
                        count += ((Collection) value).size();
                    } else if (value.getClass().isArray()) {
                        count += Array.getLength(value);
                    } else if (value instanceof Map) {
                        count += ((Map) value).size();
                    } else if (value instanceof Countable) {
                        count += ((Countable) value).size();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("get item count of result model failed, model: {}", JSON.toJSONString(model), e);
        }

        // 无容器字段时至少计为 1，保证单条结果也能触发批次逻辑
        return count > 0 ? count : 1;
    }

}
