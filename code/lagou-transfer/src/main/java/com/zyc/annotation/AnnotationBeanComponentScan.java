package com.zyc.annotation;

import java.lang.annotation.*;

/**
 * @author zhangyongchao
 * @date 2020/5/4 14:23
 * @description
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AnnotationBeanComponentScan {
    /**
     * 要扫描的包及其子包
     */
    String[] basePackages();
}
