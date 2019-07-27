package com.apartmentmanager.service.paymentwrapper;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Order {
    int value() default Integer.MAX_VALUE;
}
