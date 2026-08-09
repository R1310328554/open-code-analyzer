/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.command.annotation;

import java.lang.annotation.*;

/**
 * 命令映射注解：标注 {@link CommandHandler} 实现类对应的命令名与简要描述。
 * 由 {@link CommandHandlerProvider} 在启动时扫描并注册到命令中心路由表。
 *
 * @author Eric Zhao
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface CommandMapping {

    String name();

    /**
     * 命令简要说明，供 {@code /api} 接口列出可用命令时使用。
     *
     * @return 命令描述
     * @since 1.5.0
     */
    String desc();
}
