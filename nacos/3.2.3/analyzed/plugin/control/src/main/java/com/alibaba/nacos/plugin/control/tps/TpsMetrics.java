/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.tps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TPS 指标快照，记录某限流点在特定时间窗口内的通过/拒绝计数。
 *
 * <p>用于监控上报与日志输出，{@link #getMsg()} 以管道符拼接各维度字段。</p>
 *
 * @author shiyiyue
 */
public class TpsMetrics {
    
    /** 限流点名称。 */
    private String pointName;
    
    /** 指标类型标识。 */
    private String type;
    
    /** 统计时间戳（毫秒）。 */
    private long timeStamp;
    
    /** 统计周期（秒/分/时）。 */
    private TimeUnit period;
    
    /** 通过/拒绝计数器。 */
    private Counter counter;
    
    public TpsMetrics(String pointName, String type, long timeStamp, TimeUnit period) {
        this.pointName = pointName;
        this.type = type;
        this.timeStamp = timeStamp;
        this.period = period;
        
    }
    
    @Override
    public String toString() {
        return "TpsMetrics{" + "pointName='" + pointName + '\'' + ", type='" + type + '\''
            + ", timeStamp=" + timeStamp
            + ", period=" + period + ", counter=" + counter + '}';
    }
    
    /**
     * 将毫秒时间戳格式化为 {@code yyyy-MM-dd HH:mm:ss} 字符串。
     *
     * @param timeStamp 毫秒时间戳
     * @return 格式化后的时间字符串
     */
    public String getTimeFormatOfSecond(long timeStamp) {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeStamp));
        return format;
    }
    
    /**
     * 生成管道符分隔的指标日志行。
     *
     * @return 格式：限流点|类型|周期|时间|通过数|拒绝数
     */
    public String getMsg() {
        
        return String.join("|", pointName, type, period.name(), getTimeFormatOfSecond(timeStamp),
            String.valueOf(counter.passCount), String.valueOf(counter.deniedCount));
    }
    
    public String getPointName() {
        return pointName;
    }
    
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getTimeStamp() {
        return timeStamp;
    }
    
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    public TimeUnit getPeriod() {
        return period;
    }
    
    public void setPeriod(TimeUnit period) {
        this.period = period;
    }
    
    public Counter getCounter() {
        return counter;
    }
    
    public void setCounter(Counter counter) {
        this.counter = counter;
    }
    
    /**
     * TPS 通过/拒绝计数器。
     */
    public static class Counter {
        
        /** 通过请求计数。 */
        private long passCount;
        
        /** 被拒绝请求计数。 */
        private long deniedCount;
        
        public Counter(long passCount, long deniedCount) {
            this.passCount = passCount;
            this.deniedCount = deniedCount;
        }
        
        public long getPassCount() {
            return passCount;
        }
        
        public void setPassCount(long passCount) {
            this.passCount = passCount;
        }
        
        public long getDeniedCount() {
            return deniedCount;
        }
        
        public void setDeniedCount(long deniedCount) {
            this.deniedCount = deniedCount;
        }
        
        @Override
        public String toString() {
            return "{" + "passCount=" + passCount + ", deniedCount=" + deniedCount + '}';
        }
        
        /**
         * 生成简化的管道符分隔计数日志。
         *
         * @return 格式：通过数|拒绝数
         */
        public String getSimpleLog() {
            return String.join("|", String.valueOf(passCount), String.valueOf(deniedCount));
        }
    }
}
