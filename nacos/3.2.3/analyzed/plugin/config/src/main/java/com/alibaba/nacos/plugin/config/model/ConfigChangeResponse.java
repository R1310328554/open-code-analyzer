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

package com.alibaba.nacos.plugin.config.model;

import com.alibaba.nacos.plugin.config.constants.ConfigChangePointCutTypes;

/**
 * 配置变更插件响应模型。
 *
 * <p>插件执行后通过该对象回写成功/失败状态、返回值、消息及附加参数，
 * 供配置变更流程判断是否继续或中断。</p>
 *
 * @author liyunfei
 */
public class ConfigChangeResponse {
    
    /** 响应对应的切点类型。 */
    private ConfigChangePointCutTypes responseType;
    
    /** 插件执行是否成功。 */
    private boolean isSuccess;
    
    /** 插件返回的业务结果。 */
    private Object retVal;
    
    /** 插件返回的消息文本。 */
    private String msg;
    
    /** 插件附加参数数组。 */
    private Object[] args;
    
    /**
     * 构造指定切点类型的配置变更响应。
     *
     * @param responseType 切点类型
     */
    public ConfigChangeResponse(ConfigChangePointCutTypes responseType) {
        this.responseType = responseType;
    }
    
    /**
     * 获取响应切点类型。
     *
     * @return 切点类型
     */
    public ConfigChangePointCutTypes getResponseType() {
        return responseType;
    }
    
    /**
     * 设置响应切点类型。
     *
     * @param responseType 切点类型
     */
    public void setResponseType(ConfigChangePointCutTypes responseType) {
        this.responseType = responseType;
    }
    
    /**
     * 判断插件执行是否成功。
     *
     * @return 成功为 {@code true}
     */
    public boolean isSuccess() {
        return isSuccess;
    }
    
    /**
     * 设置插件执行结果。
     *
     * @param success 是否成功
     */
    public void setSuccess(boolean success) {
        isSuccess = success;
    }
    
    /**
     * 获取插件返回的业务结果。
     *
     * @return 返回值对象
     */
    public Object getRetVal() {
        return retVal;
    }
    
    /**
     * 设置插件返回的业务结果。
     *
     * @param retVal 返回值对象
     */
    public void setRetVal(Object retVal) {
        this.retVal = retVal;
    }
    
    /**
     * 获取插件返回的消息。
     *
     * @return 消息文本
     */
    public String getMsg() {
        return msg;
    }
    
    /**
     * 设置插件返回的消息。
     *
     * @param msg 消息文本
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    /**
     * 获取插件附加参数数组。
     *
     * @return 参数数组
     */
    public Object[] getArgs() {
        return args;
    }
    
    /**
     * 设置插件附加参数数组。
     *
     * @param args 参数数组
     */
    public void setArgs(Object[] args) {
        this.args = args;
    }
}
