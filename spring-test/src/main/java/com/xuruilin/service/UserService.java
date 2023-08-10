package com.xuruilin.service;

import com.spring.Autowired;
import com.spring.Component;
import com.spring.InitializingBean;
import com.spring.Scope;

@Component("userService")
@Scope("singleton")
public class UserService implements InitializingBean,UserInterface {

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println(orderService);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("初始化");
    }
}
