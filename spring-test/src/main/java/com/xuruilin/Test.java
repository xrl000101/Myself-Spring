package com.xuruilin;


import com.spring.MyselfApplicationContext;
import com.xuruilin.service.UserInterface;
import com.xuruilin.service.UserService;

import java.lang.reflect.InvocationTargetException;

/**
 * 模拟手写Spring框架  理解Spring
 */
public class Test {

	public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

		//扫描获得哪些是单例bean ->创建单例bean
		MyselfApplicationContext myselfApplicationContext = new MyselfApplicationContext(AppConfig.class);

		UserInterface bean = (UserInterface) myselfApplicationContext.getBean("userService");
		System.out.println(bean);
		bean.test();
	}
}







