package com.zyc.annotation;

import java.lang.annotation.*;

/**
 * @author zhangyongchao
 * @date 2020/5/4 23:01
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    String value() default "";
}
