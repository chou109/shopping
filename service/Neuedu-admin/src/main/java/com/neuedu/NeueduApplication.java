package com.neuedu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author neuedu
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class }, scanBasePackages = {"${ruoyi.auto-scan-package}"})
public class NeueduApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(NeueduApplication.class, args);
    }
}
