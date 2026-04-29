package com.codbid.slo.checker;

import com.codbid.slo.checker.config.SloProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties(SloProperties.class)
@SpringBootApplication
public class SloCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SloCheckerApplication.class, args);
    }

}
