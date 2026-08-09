package com.taobao.arthas.core.command;

import com.taobao.arthas.core.advisor.Advice;

/**
 * 脚本增强命令的公共契约：定义脚本生命周期回调与输出接口，供 watch/trace 等命令的 Groovy 脚本使用。
 * <p>
 * 脚本支持命令
 * Created by vlinux on 15/6/1.
 */
public interface ScriptSupportCommand {

    /**
     * 增强脚本监听器：在方法进入、正常返回、异常抛出等切点触发用户脚本。
     */
    interface ScriptListener {

        /**
         * 脚本创建
         *
         * @param output 输出器
         */
        void create(Output output);

        /**
         * 脚本销毁
         *
         * @param output 输出器
         */
        void destroy(Output output);

        /**
         * 方法执行前
         *
         * @param output 输出器
         * @param advice 通知点
         */
        void before(Output output, Advice advice);

        /**
         * 方法正常返回
         *
         * @param output 输出器
         * @param advice 通知点
         */
        void afterReturning(Output output, Advice advice);

        /**
         * 方法异常返回
         *
         * @param output 输出器
         * @param advice 通知点
         */
        void afterThrowing(Output output, Advice advice);

    }

    /**
     * 脚本监听器适配器：空实现，子类按需覆盖感兴趣的生命周期方法。
     */
    class ScriptListenerAdapter implements ScriptListener {

        @Override
        public void create(Output output) {

        }

        @Override
        public void destroy(Output output) {

        }

        @Override
        public void before(Output output, Advice advice) {

        }

        @Override
        public void afterReturning(Output output, Advice advice) {

        }

        @Override
        public void afterThrowing(Output output, Advice advice) {

        }
    }


    /**
     * 脚本输出器：向终端或结果流写入文本，并支持提前结束脚本执行。
     */
    interface Output {

        /**
         * 输出字符串(不换行)
         *
         * @param string 待输出字符串
         * @return this
         */
        Output print(String string);

        /**
         * 输出字符串(换行)
         *
         * @param string 待输出字符串
         * @return this
         */
        Output println(String string);

        /**
         * 结束当前脚本
         *
         * @return this
         */
        Output finish();

    }

}
