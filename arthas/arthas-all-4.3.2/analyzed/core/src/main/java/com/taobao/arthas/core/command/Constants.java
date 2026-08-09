package com.taobao.arthas.core.command;

/**
 * 命令模块公共字符串常量：帮助文案、表达式说明、Wiki 链接等，供各 {@link com.taobao.arthas.core.shell.command.AnnotatedCommand} 复用。
 *
 * @author ralf0131 2016-12-14 17:21.
 * @author hengyunabc 2018-12-03
 */
public interface Constants {

    /**
     * watch/trace 等命令中 OGNL 表达式的可用变量说明（运行时动态求值）。
     * TODO improve the description
     */
    String EXPRESS_DESCRIPTION = "  The express may be one of the following expression (evaluated dynamically):\n" +
            "          target : the object\n" +
            "           clazz : the object's class\n" +
            "          method : the constructor or method\n" +
            "          params : the parameters array of method\n" +
            "    params[0..n] : the element of parameters array\n" +
            "       returnObj : the returned object of method\n" +
            "        throwExp : the throw exception of method\n" +
            "        isReturn : the method ended by return\n" +
            "         isThrow : the method ended by throwing exception\n" +
            "           #cost : the execution time in ms of method invocation";

    /** 帮助文本中的示例段落标题 */
    String EXAMPLE = "\nEXAMPLES:\n";

    /** 帮助文本中的 Wiki 段落标题 */
    String WIKI = "\nWIKI:\n";

    /** Arthas 官方文档首页地址 */
    String WIKI_HOME = "  https://arthas.aliyun.com/doc/";

    /** OGNL 表达式常用示例列表 */
    String EXPRESS_EXAMPLES =   "Examples:\n" +
                                "  params\n" +
                                "  params[0]\n" +
                                "  'params[0]+params[1]'\n" +
                                "  '{params[0], target, returnObj}'\n" +
                                "  returnObj\n" +
                                "  throwExp\n" +
                                "  target\n" +
                                "  clazz\n" +
                                "  method\n";

    /** watch/trace 等命令的条件表达式（OGNL 风格）说明与示例 */
    String CONDITION_EXPRESS =  "Conditional expression in ognl style, for example:\n" +
                                "  TRUE  : 1==1\n" +
                                "  TRUE  : true\n" +
                                "  FALSE : false\n" +
                                "  TRUE  : 'params.length>=0'\n" +
                                "  FALSE : 1==2\n" +
                                "  '#cost>100'\n";

}
