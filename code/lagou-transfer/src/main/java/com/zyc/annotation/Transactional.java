package com.zyc.annotation;

import java.lang.annotation.*;

/**
 * @author zhangyongchao
 * @date 2020/5/5 10:21
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Transactional {
}
