package com.pinyougou.manager.controller;

import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;

public class Test2 {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        DemoTest demoTest = new DemoTest();

        Class<? extends DemoTest> clazz = demoTest.getClass();
        DemoTest test = clazz.newInstance();

    }

}
