package com.nhnacademy.proxyexample.config;

import com.nhnacademy.proxyexample.Base;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = Base.class)
public class RootConfig {

}
