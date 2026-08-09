/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.spring.webmvc.controller;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;

/**
 * WebMvc 限流演示控制器：提供 hello、foo、async 等测试接口。
 *
 * @author kaizi2009
 */
@Controller
public class WebMvcTestController {

    @GetMapping("/hello")
    @ResponseBody
    /** GET /hello：简单问候接口。 */
    public String apiHello() {
        doBusiness();
        return "Hello!";
    }

    @GetMapping("/err")
    @ResponseBody
    /** GET /err：模拟业务错误响应。 */
    public String apiError() {
        doBusiness();
        return "Oops...";
    }

    @GetMapping("/foo/{id}")
    @ResponseBody
    /** GET /foo/{id}：带路径参数的问候接口。 */
    public String apiFoo(@PathVariable("id") Long id) {
        doBusiness();
        return "Hello " + id;
    }

    @GetMapping("/exclude/{id}")
    @ResponseBody
    /** GET /exclude/{id}：用于排除限流的测试接口。 */
    public String apiExclude(@PathVariable("id") Long id) {
        doBusiness();
        return "Exclude " + id;
    }

    @GetMapping("/forward")
    /** GET /forward：视图转发演示。 */
    public ModelAndView apiForward() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("hello");
        return mav;
    }

    @GetMapping("/async")
    @ResponseBody
    /** GET /async：DeferredResult 异步响应演示。 */
    public DeferredResult<String> distribute() throws Exception {
        DeferredResult<String> result = new DeferredResult<>(4000L);

        Thread thread = new Thread(() -> result.setResult("async result"));
        thread.start();

        return result;
    }

    /** 模拟随机 0~100ms 业务耗时。 */
    private void doBusiness() {
        Random random = new Random(1);
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
