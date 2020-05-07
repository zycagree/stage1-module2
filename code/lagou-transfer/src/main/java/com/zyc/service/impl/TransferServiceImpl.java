package com.zyc.service.impl;

import com.zyc.annotation.Autowired;
import com.zyc.annotation.Service;
import com.zyc.annotation.Transactional;
import com.zyc.dao.AccountDao;
import com.zyc.dao.pojo.Account;
import com.zyc.service.TransferService;

/**
 * @author zhangyongchao
 * @date 2020/5/4 12:44
 * @description
 */
@Service
@Transactional
public class TransferServiceImpl implements TransferService {

    @Autowired
    private AccountDao accountDao;

    @Override
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {
        Account from = accountDao.queryAccountByCardNo(fromCardNo);
        Account to = accountDao.queryAccountByCardNo(toCardNo);

        from.setMoney(from.getMoney() - money);
        to.setMoney(to.getMoney() + money);

        accountDao.updateAccountByCardNo(to);
//        int c = 1/0;
        accountDao.updateAccountByCardNo(from);

    }

}
