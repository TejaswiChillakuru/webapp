package com.neu.edu.assignment2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class UserConfig {
    @Bean
    public Docket postsApi(){
        return new Docket(DocumentationType.SWAGGER_2).groupName("java techie").apiInfo(apiInfo()).select().paths(regex("/v1/user.*")).build();
    }
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder().title("User Service")
                .description("Sample Documentation Generated Using SWAGGER2 for our Rest Api")
                //.termsOfServiceUrl("https://www.youtube.com/channel/UCORuRdpN2QTCKnsuEaeK-kQ")
                //.license("Java_Gyan_Mantra_License")
                //.licenseUrl("https://www.youtube.com/channel/UCORuRdpN2QTCKnsuEaeK-kQ")
                .version("1.0").build();
    }
}
