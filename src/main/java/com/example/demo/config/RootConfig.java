package com.example.demo.config;

import com.example.demo.Base;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = Base.class)
public class RootConfig {

}
