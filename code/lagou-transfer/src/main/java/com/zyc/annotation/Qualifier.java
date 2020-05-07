package com.zyc.annotation;

import java.lang.annotation.*;

/**
 * @author zhangyongchao
 * @date 2020/5/4 23:07
 * @description
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Qualifier {
    String value() default "";
}
