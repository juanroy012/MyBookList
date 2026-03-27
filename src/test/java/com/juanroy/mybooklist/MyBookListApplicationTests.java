package com.juanroy.mybooklist;

import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;

@Suite
@SelectPackages("com.juanroy.mybooklist")
@SpringBootTest
class MyBookListApplicationTests {

    @Test
    void contextLoads() {
    }

}
