/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.spring.data.connection;

import org.redisson.client.protocol.convertor.Convertor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metric;

/**
 * 将 Redis GEO 距离数值包装为 Spring {@link Distance}。
 * <p>构造时指定 {@link Metric}（如千米、英里），供 {@code GEODIST} 等命令使用。
 *
 * @author Nikita Koksharov
 *
 */
public class DistanceConvertor implements Convertor<Distance> {

    private final Metric metric;
    
    /** 绑定距离度量单位。 */
    public DistanceConvertor(Metric metric) {
        super();
        this.metric = metric;
    }

    /** 将 {@link Double} 数值与 {@code metric} 组装为 {@link Distance}。 */
    @Override
    public Distance convert(Object obj) {
        return new Distance((Double)obj, metric);
    }

}
