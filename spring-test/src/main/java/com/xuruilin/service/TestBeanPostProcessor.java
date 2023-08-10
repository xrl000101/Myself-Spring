package com.xuruilin.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class TestBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")){
            System.out.println("postProcessBeforeInitialization");
            Object instance = Proxy.newProxyInstance(TestBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    //代理AOP，实现切面逻辑
                    System.out.println("切面逻辑");
                    return method.invoke(bean,args);
                }
            });
            return instance;
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("postProcessAfterInitialization");
        return bean;
    }
}
