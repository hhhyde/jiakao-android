package com.hqhcn.android;

import com.hqhcn.android.tool.swagger.config.SwaggerConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author 柯江涛
 */
@Import({SwaggerConfig.class})
@Controller
@EnableSwagger2
@MapperScan("com.hqhcn.android.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class Application {

    @Bean
    @LoadBalanced()
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced()
    AsyncRestTemplate asyncRestTemplate(){
        return new AsyncRestTemplate();
    }

//    @Bean
//    public IRule ribbonRule(){
//        // 简单粗暴 随机负载
    // TODO: 2018/1/20 0020 选择随机和最优策略之后,请求的服务端报错一次后就找不到服务了
//		return new RandomRule();
//
//        // 负载均衡,遍历每个服务,取最小的并发数服务
////        return new BestAvailableRule();
//    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("Service startup complete!");
    }
}
