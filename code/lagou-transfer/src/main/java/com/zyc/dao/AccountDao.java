package com.zyc.dao;

import com.zyc.dao.pojo.Account;

/**
 * @author zhangyongchao
 * @date 2020/5/4 12:33
 * @description
 */
public interface AccountDao {

    Account queryAccountByCardNo(String cardNo) throws Exception;

    int updateAccountByCardNo(Account account) throws Exception;

}
