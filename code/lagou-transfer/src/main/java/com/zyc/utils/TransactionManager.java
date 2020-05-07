package com.zyc.utils;

import com.zyc.annotation.Autowired;
import com.zyc.annotation.Component;

import java.sql.SQLException;

/**
 * @author zhangyongchao
 * @date 2020/5/4 12:52
 * @description
 */
@Component
public class TransactionManager {

    @Autowired
    private ConnectionUtils connectionUtils;

    // 开启手动事务控制
    public void beginTransaction() throws SQLException {
        connectionUtils.getCurrentThreadConn().setAutoCommit(false);
    }


    // 提交事务
    public void commit() throws SQLException {
        connectionUtils.getCurrentThreadConn().commit();
    }


    // 回滚事务
    public void rollback() throws SQLException {
        connectionUtils.getCurrentThreadConn().rollback();
    }

}
