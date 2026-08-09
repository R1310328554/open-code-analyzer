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
package com.alibaba.csp.sentinel.node.metric;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 从指标日志文件读取 {@link MetricNode} 数据。
 */
class MetricsReader {

    /**
     * 避免任何情况下 OOM。
     */
    private static final int MAX_LINES_RETURN = 100000;

    private final Charset charset;

    public MetricsReader(Charset charset) {
        this.charset = charset;
    }

    /**
     * @return 若应继续读取返回 true，否则 false
     */
    boolean readMetricsInOneFileByEndTime(List<MetricNode> list, String fileName, long offset,
                                          long beginTimeMs, long endTimeMs, String identity) throws Exception {
        FileInputStream in = null;
        long beginSecond = beginTimeMs / 1000;
        long endSecond = endTimeMs / 1000;
        try {
            in = new FileInputStream(fileName);
            in.getChannel().position(offset);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                MetricNode node = MetricNode.fromFatString(line);
                long currentSecond = node.getTimestamp() / 1000;
                // currentSecond 应 >= beginSecond，否则说明指标文件异常
                if (currentSecond < beginSecond) {
                    return false;
                }
                if (currentSecond <= endSecond) {
                    // 读取全部
                    if (identity == null) {
                        list.add(node);
                    } else if (node.getResource().equals(identity)) {
                        list.add(node);
                    }
                } else {
                    return false;
                }
                if (list.size() >= MAX_LINES_RETURN) {
                    return false;
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return true;
    }

    void readMetricsInOneFile(List<MetricNode> list, String fileName,
                              long offset, int recommendLines) throws Exception {
        //if(list.size() >= recommendLines){
        //    return;
        //}
        long lastSecond = -1;
        if (list.size() > 0) {
            lastSecond = list.get(list.size() - 1).getTimestamp() / 1000;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            in.getChannel().position(offset);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            String line;
            while ((line = reader.readLine()) != null) {
                MetricNode node = MetricNode.fromFatString(line);
                long currentSecond = node.getTimestamp() / 1000;

                if (list.size() < recommendLines) {
                    list.add(node);
                } else if (currentSecond == lastSecond) {
                    list.add(node);
                } else {
                    break;
                }
                lastSecond = currentSecond;
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * identity 为 null 时读取时间区间内全部指标，否则仅读取指定资源。
     */
    List<MetricNode> readMetricsByEndTime(List<String> fileNames, int pos, long offset,
                                          long beginTimeMs, long endTimeMs, String identity) throws Exception {
        List<MetricNode> list = new ArrayList<MetricNode>(1024);
        if (readMetricsInOneFileByEndTime(list, fileNames.get(pos++), offset, beginTimeMs, endTimeMs, identity)) {
            while (pos < fileNames.size()
                && readMetricsInOneFileByEndTime(list, fileNames.get(pos++), 0, beginTimeMs, endTimeMs, identity)) {
            }
        }
        return list;
    }

    List<MetricNode> readMetrics(List<String> fileNames, int pos,
                                 long offset, int recommendLines) throws Exception {
        List<MetricNode> list = new ArrayList<MetricNode>(recommendLines);
        readMetricsInOneFile(list, fileNames.get(pos++), offset, recommendLines);
        while (list.size() < recommendLines && pos < fileNames.size()) {
            readMetricsInOneFile(list, fileNames.get(pos++), 0, recommendLines);
        }
        return list;
    }
}