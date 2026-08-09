/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.jaxrs;

/**
 * JAX-RS 演示用简单 POJO：携带 id 与 msg 字段。
 *
 * @author sea
 */
public class HelloEntity {

    Long id;

    String msg;

    /** 无参构造。 */
    public HelloEntity() {
    }

    /** 仅设置 msg 的构造。 */
    public HelloEntity(String msg) {
        this.msg = msg;
    }

    /** 设置 id 与 msg 的构造。 */
    public HelloEntity(Long id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    /** 返回实体 id。 */
    public Long getId() {
        return id;
    }

    /** 设置实体 id。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 返回消息内容。 */
    public String getMsg() {
        return msg;
    }

    /** 设置消息内容。 */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "HelloEntity{" +
                "id=" + id +
                ", msg='" + msg + '\'' +
                '}';
    }
}
