/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common.attribute;

/**
 * Topic/消费组等资源的命名属性抽象基类。
 * 子类实现 {@link #verify(String)} 校验取值，{@link #changeable} 控制是否允许运行时修改。
 */
public abstract class Attribute {
    /** 属性名（如 queue.type、message.type）。 */
    protected String name;
    /** 创建后是否允许变更。 */
    protected boolean changeable;

    /** 校验属性值是否合法，非法时抛出异常。 */
    public abstract void verify(String value);

    /** 构造属性定义。 */
    public Attribute(String name, boolean changeable) {
        this.name = name;
        this.changeable = changeable;
    }

    /** 属性名。 */
    public String getName() {
        return name;
    }

    /** 设置属性名。 */
    public void setName(String name) {
        this.name = name;
    }

    /** 是否可在运行时修改。 */
    public boolean isChangeable() {
        return changeable;
    }

    /** 设置是否可变更。 */
    public void setChangeable(boolean changeable) {
        this.changeable = changeable;
    }
}
