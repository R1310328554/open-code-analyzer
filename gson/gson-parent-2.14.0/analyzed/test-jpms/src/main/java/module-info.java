/*
 * Copyright (C) 2024 Google Inc.
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

/**
 * 占位模块，避免仅在测试源码中存在 {@code module-info.java} 时 Maven Compiler Plugin 报错：
 *
 * <blockquote>
 *
 * Can't compile test sources when main sources are missing a module descriptor
 *
 * </blockquote>
 *
 * <p>主源码缺少模块描述符时无法编译测试源码；此空模块满足编译器要求。
 */
module com.google.gson.dummy {}
