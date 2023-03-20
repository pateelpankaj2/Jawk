package com.mpay.configuration;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EmailConfiguration {

    @Value(value = "${system.email.num-concurrent-emails:2}")
    int numConcurrentEmails;

    @Bean(name = "emailThreadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(numConcurrentEmails);
        taskExecutor.setTaskDecorator(new MdcTaskDecorator());
        return taskExecutor;
    }
}
