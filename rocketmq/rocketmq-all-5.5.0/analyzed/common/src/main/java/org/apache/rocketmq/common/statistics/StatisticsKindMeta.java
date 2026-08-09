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
package org.apache.rocketmq.common.statistics;

/**
 * 统计类别元数据：名称、子项列表及关联的定时打印机。
 */
public class StatisticsKindMeta {
    /** 统计类别名。 */
    private String name;
    /** 该类别下各子项名称。 */
    private String[] itemNames;
    /** 该类别统计项注册时使用的定时打印机。 */
    private StatisticsItemScheduledPrinter scheduledPrinter;

    /** 返回类别名。 */
    public String getName() {
        return name;
    }

    /** 设置类别名。 */
    public void setName(String name) {
        this.name = name;
    }

    /** 返回子项名称数组。 */
    public String[] getItemNames() {
        return itemNames;
    }

    /** 设置子项名称数组。 */
    public void setItemNames(String[] itemNames) {
        this.itemNames = itemNames;
    }

    /** 返回定时打印机。 */
    public StatisticsItemScheduledPrinter getScheduledPrinter() {
        return scheduledPrinter;
    }

    /** 设置定时打印机。 */
    public void setScheduledPrinter(StatisticsItemScheduledPrinter scheduledPrinter) {
        this.scheduledPrinter = scheduledPrinter;
    }
}