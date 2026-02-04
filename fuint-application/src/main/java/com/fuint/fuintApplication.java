package com.fuint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 启动程序
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@EnableScheduling
@SpringBootApplication
//@PropertySource("file:${env.properties.path}/${env.profile}/application.properties")
public class fuintApplication {


    @Bean
    public FilterRegistrationBean cross() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        //注入过滤器
        registrationBean.setFilter((servletRequest, servletResponse, filterChain) -> {
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            //OPTIONS请求用于跨域时，浏览器用于预检内容，一般响应OPTIONS请求正常即可。反正还是要具体情况具体实现

            //httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
            //有时候直接用*会导致范围过大，浏览器出于安全考虑，有时候会不认*这个操作，因此可以使用如下代码，间接实现允许跨域
            httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("origin"));
            //响应头设置
            httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
            //响应类型
            httpServletResponse.setHeader("Access-Control-Allow-Methods", "*");
            //允许跨越发送cookie
            httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
            if (httpServletRequest.getMethod().equalsIgnoreCase("options")) {
                //在携带头特殊的请求头并且非同源的条件下，会发送预检请求。
                //通过响应头告诉预检请求，我是支持跨域的。并且返回状态码是200
                httpServletResponse.setStatus(200);
                return;
            }
            filterChain.doFilter(servletRequest, servletResponse);
        });
        //过滤器名称
        registrationBean.setName("CrossOrigin");
        //拦截规则
        registrationBean.addUrlPatterns("/*");
        //过滤器顺序
        registrationBean.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);

        return registrationBean;
    }


    public static final String REWRITE_FILTER_NAME = "rewriteFilter";
    public static final String REWRITE_FILTER_CONF_PATH = "urlRewrite.xml";



    public static void main(String[] args) {
        SpringApplication.run(fuintApplication.class, args);
        System.out.println("==================================================\n" +
                "恭喜，fuint系统启动成功啦！  \n" +
                "系统官网：https://www.fuint.cn  \n" +
                "接口文档：http://localhost:7800/swagger-ui.html \n" +
                "==================================================\n  \n");
    }

    @Bean
    public FilterRegistrationBean rewriteFilterConfig() {
        FilterRegistrationBean reg = new FilterRegistrationBean();
        reg.setName(REWRITE_FILTER_NAME);
        reg.setFilter(new UrlRewriteFilter());
        reg.addInitParameter("confPath", REWRITE_FILTER_CONF_PATH);
        reg.addInitParameter("confReloadCheckInterval", "-1");
        reg.addInitParameter("statusPath", "/redirect");
        reg.addInitParameter("statusEnabledOnHosts", "*");
        reg.addInitParameter("logLevel", "WARN");
        return reg;
    }
}
