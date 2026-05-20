package com.spintale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * SpinTale backend application.
 *
 * @author spintale
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class SpinTaleApplication
{
    private static final Logger log = LoggerFactory.getLogger(SpinTaleApplication.class);
    
    public static void main(String[] args)
    {
        SpringApplication.run(SpinTaleApplication.class, args);
        log.info("SpinTale backend started successfully.");
    }
}
