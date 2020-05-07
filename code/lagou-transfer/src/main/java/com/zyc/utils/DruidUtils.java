package com.zyc.utils;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @author zhangyongchao
 * @date 2020/5/4 12:50
 * @description
 */
public class DruidUtils {


    private DruidUtils(){
    }

    private static DruidDataSource druidDataSource = new DruidDataSource();


    static {
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/bank");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("root");

    }

    public static DruidDataSource getInstance() {
        return druidDataSource;
    }


}
