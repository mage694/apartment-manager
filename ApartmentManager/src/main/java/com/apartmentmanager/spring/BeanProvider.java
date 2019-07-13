package com.apartmentmanager.spring;

import com.google.common.collect.Sets;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Component
public class BeanProvider implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @SafeVarargs
    public final <T> Collection<T> getBeans(Class<T> type, T... exclusions) {
        if (exclusions == null) {
            return applicationContext.getBeansOfType(type).values();
        } else {
            Set<? extends T> exclusionSet = Sets.newHashSet(exclusions);
            return applicationContext.getBeansOfType(type).values().stream()
                    .filter(c -> !exclusionSet.contains(c))
                    .collect(Collectors.toSet());
        }

    }

    public final <T> T getBean(Class<T> type) {
        return applicationContext.getBean(type);
    }

    public final <T> T getBean(Class<T> type, String beanName) {
        return applicationContext.getBean(type, beanName);
    }
}
