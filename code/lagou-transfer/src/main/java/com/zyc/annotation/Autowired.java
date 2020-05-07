package com.zyc.annotation;

import java.lang.annotation.*;

/**
 * @author zhangyongchao
 * @date 2020/5/4 23:05
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowired {
}
