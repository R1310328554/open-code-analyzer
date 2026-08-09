package com.alibaba.arthas;

/**
 * 简单 POJO，供 Arthas 命令测试用例构造对象图与属性访问场景。
 */
public class Pojo {
    String name;
    int age;
    String hobby;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }
}
