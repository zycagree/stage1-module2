package com.zyc.exception;

/**
 * @author zhangyongchao
 * @date 2020/5/5 8:08
 * @description
 */
public class NoBeanDefinitionException extends RuntimeException{

    public NoBeanDefinitionException(String msg){
        super(msg);
    }

    public NoBeanDefinitionException(String msg, Throwable cause){
        super(msg, cause);
    }

}
