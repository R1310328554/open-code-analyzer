/*
 * Copyright (C) 2021 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gson.metrics;

import com.google.caliper.runner.CaliperMain;

/** 运行 Caliper 基准测试但不将结果上传到远程服务器的辅助类。 */
class NonUploadingCaliperRunner {
  private NonUploadingCaliperRunner() {}

  /** 将首个参数与后续参数拼接为新的命令行参数数组。 */
  private static String[] concat(String first, String... others) {
    if (others.length == 0) {
      return new String[] {first};
    } else {
      String[] result = new String[others.length + 1];
      result[0] = first;
      System.arraycopy(others, 0, result, 1, others.length);
      return result;
    }
  }

  /** 以禁用结果上传的方式启动指定基准测试类。 */
  public static void run(Class<?> c, String[] args) {
    // 禁用结果上传；Caliper 默认会上传结果，见 https://github.com/google/caliper/issues/356
    CaliperMain.main(c, concat("-Cresults.upload.options.url=", args));
  }
}
