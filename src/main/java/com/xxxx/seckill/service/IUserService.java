package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther:huhao
 * @Date:2021/8/18-21:47
 * @Description:com.xxxx.seckill.controller
 * @version:1.0
 */
public interface IUserService extends IService<User> {


	RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);


	User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);


}