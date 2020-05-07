package com.zyc.factory;

import com.zyc.annotation.Autowired;
import com.zyc.annotation.Component;
import com.zyc.annotation.Transactional;
import com.zyc.utils.TransactionManager;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

/**
 * @author zhangyongchao
 * @date 2020/5/4 12:38
 * @description
 */
@Component
public class ProxyFactory {

    @Autowired
    private TransactionManager transactionManager;

    /**
     * Jdk动态代理
     * @param obj  委托对象
     * @return   代理对象
     */
    public Object getJdkProxy(Object obj) {

        // 获取代理对象
        return  Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 如果未开启事务（类和方法上均为注解@Transactional）则不加事务功能
                        if (!injectTransactionManager(obj, method)){
                            return method.invoke(obj, args);
                        }
                        // 加入事务功能
                        return invokeWithTransaction(method, args, obj);
                    }
                });

    }

    /**
     * 使用cglib动态代理生成代理对象
     * @param obj 委托对象
     * @return
     */
    public Object getCglibProxy(Object obj) {
        return  Enhancer.create(obj.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                // 如果未开启事务（类和方法上均为注解@Transactional）则不加事务功能
                if (!injectTransactionManager(obj, method)){
                    return method.invoke(obj, objects);
                }
                // 加入事务功能
                return invokeWithTransaction(method, objects, obj);
            }
        });
    }

    private Object invokeWithTransaction(Method method, Object[] args, Object obj) throws SQLException, IllegalAccessException, InvocationTargetException {
        Object result = null;
        try {
            // 开启事务(关闭事务的自动提交)
            transactionManager.beginTransaction();

            result = method.invoke(obj, args);

            // 提交事务
            transactionManager.commit();
        } catch (Exception e) {
            e.printStackTrace();
            // 回滚事务
            transactionManager.rollback();
            // 抛出异常便于上层servlet捕获
            throw e;

        }
        return result;
    }

    private boolean injectTransactionManager(Object obj, Method method) throws NoSuchMethodException {
        Class<?> objClass = obj.getClass();
        return transactionalOnClass(objClass) || transactionalOnMethod(method, objClass);
    }

    private boolean transactionalOnMethod(Method method, Class<?> objClass) throws NoSuchMethodException {
        return objClass.getMethod(method.getName(), method.getParameterTypes()).isAnnotationPresent(Transactional.class);
    }

    private boolean transactionalOnClass(Class<?> objClass) {
        return objClass.isAnnotationPresent(Transactional.class);
    }
}

