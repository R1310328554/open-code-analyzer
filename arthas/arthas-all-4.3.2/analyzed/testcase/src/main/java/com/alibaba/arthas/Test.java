package com.alibaba.arthas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Arthas 功能验证用的示例程序：维护静态 Map、循环构造 {@link Pojo} 列表并周期性调用测试方法。
 *
 * @author diecui1202 on 2017/9/13.
 */
public class Test {

    /** 普通静态 HashMap，用于测试 getstatic/ognl 等命令 */
    public static final Map m = new HashMap();
    /** 以 {@link Type} 枚举为 key 的静态 Map */
    public static final Map n = new HashMap();

    /** 故意置 null，用于测试空引用场景 */
    public static final Map p = null;

    static {
        m.put("a", "aaa");
        m.put("b", "bbb");

        n.put(Type.RUN, "aaa");
        n.put(Type.STOP, "bbb");
    }

    /** 持续随机修改列表元素并调用 {@link #test(List)}，便于 watch/trace 等命令观测 */
    public static void main(String[] args) throws InterruptedException {
        List<Pojo> list = new ArrayList<Pojo>();

        for (int i = 0; i < 40; i ++) {
            Pojo pojo = new Pojo();
            pojo.setName("name " + i);
            pojo.setAge(i + 2);

            list.add(pojo);
        }

        System.out.println(p);

        while (true) {
            int random = new Random().nextInt(40);
            String name = list.get(random).getName();
            list.get(random).setName(null);
            test(list);
            list.get(random).setName(name);
            Thread.sleep(1000L);
        }
    }

    /** 空方法体，作为 trace/watch 的目标切入点 */
    public static void test(List<Pojo> list) {
        // do nothing
    }

    /** 简单打印入参，用于测试 method 调用链观测 */
    public static void invoke(String a) {
        System.out.println(a);
    }
}
