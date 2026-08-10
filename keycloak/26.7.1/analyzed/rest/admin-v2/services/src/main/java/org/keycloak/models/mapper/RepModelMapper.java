package org.keycloak.models.mapper;

import java.util.Set;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 * REST 表示类型 T 与领域模型 U 之间的双向映射通用接口。
 */
public interface RepModelMapper <T, U> {
    
    /** 全字段从模型转为表示（等价于 {@code includeFields=null}）。 */
    default T fromModel(U model) {
        return fromModel(model, null);
    }
    
    T fromModel(U model, Set<String> includeFields);
    
    void toModel(T rep, U existingModel);
}
