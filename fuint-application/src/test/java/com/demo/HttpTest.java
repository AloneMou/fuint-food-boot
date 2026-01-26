package com.demo;

import cn.hutool.core.lang.RegexPool;
import cn.hutool.core.util.ReUtil;

/**
 * @author Miao
 * @date 2026/1/19
 */
public class HttpTest {

    public static void main(String[] args) {
        System.out.println(ReUtil.isMatch(RegexPool.URL, "https://www.fuint.cn"));
        System.out.println(ReUtil.isMatch(RegexPool.URL, "http://www.fuint.cn"));
        System.out.println(ReUtil.isMatch(RegexPool.URL, "/images/p.png"));
    }
}
