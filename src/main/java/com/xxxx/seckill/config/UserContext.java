package com.xxxx.seckill.config;

import com.xxxx.seckill.pojo.User;

/**
 * @Auther:huhao
 * @Date:2021/8/25-08-25-15:56
 * @Description:com.xxxx.seckill.config
 * @version:1.0
 */
public class UserContext {
    private static ThreadLocal<User> userHolder=new ThreadLocal<User>();

    public static void setUser(User user){
        userHolder.set(user);
    }
    public static User getUser(){
        return userHolder.get();
    }
}
