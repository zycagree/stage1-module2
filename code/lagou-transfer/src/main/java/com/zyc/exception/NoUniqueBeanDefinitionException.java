package com.zyc.exception;

/**
 * @author zhangyongchao
 * @date 2020/5/5 8:08
 * @description
 */
public class NoUniqueBeanDefinitionException extends RuntimeException{

    public NoUniqueBeanDefinitionException(String msg){
        super(msg);
    }

    public NoUniqueBeanDefinitionException(String msg, Throwable cause){
        super(msg, cause);
    }

}
