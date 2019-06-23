package com.apartmentmanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadExecutorConfiguration implements AsyncConfigurer {

    @Value("${async.executor.namePrefix}")
    private String namePrefix;
    @Value("${async.executor.aliveSeconds}")
    private int aliveSeconds;
    @Value("${async.executor.maxPoolSize}")
    private int maxPoolSize;
    @Value("${async.executor.corePoolSize}")
    private int corePoolSize;

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setKeepAliveSeconds(aliveSeconds);
        executor.setThreadNamePrefix(namePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    private class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        private Logger LOGGER = LoggerFactory.getLogger(AsyncExceptionHandler.class);

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
            LOGGER.error(method.getName() + " error:", throwable);
        }

    }
}
