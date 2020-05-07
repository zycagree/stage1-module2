package com.zyc.service;

/**
 * @author zhangyongchao
 * @date 2020/5/4 12:42
 * @description
 */
public interface TransferService {

    void transfer(String fromCardNo,String toCardNo,int money) throws Exception;

}
