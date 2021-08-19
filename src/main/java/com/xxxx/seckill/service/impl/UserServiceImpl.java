package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.mapper.UserMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IUserService;
import com.xxxx.seckill.utils.CookieUtil;
import com.xxxx.seckill.utils.MD5Util;
import com.xxxx.seckill.utils.UUIDUtil;
import com.xxxx.seckill.vo.LoginVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @Auther:huhao
 * @Date:2021/8/18-21:47
 * @Description:com.xxxx.seckill.controller
 * @version:1.0
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

	@Resource
	private UserMapper userMapper;
	@Autowired
	private RedisTemplate redisTemplate;



	@Override
	public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
		String mobile = loginVo.getMobile();
		String password = loginVo.getPassword();

		User user = userMapper.selectById(mobile);
		if (null == user) {
			throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
		}
		//判断密码是否正确
		if (!MD5Util.formPassToDBPass(password, user.getSlat()).equals(user.getPassword())) {
			throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
		}
		//生成cookie
		String ticket = UUIDUtil.uuid();
		//将用户信息存入redis中
		redisTemplate.opsForValue().set("user:" + ticket, user);
		CookieUtil.setCookie(request, response, "userTicket", ticket);
		return RespBean.success(ticket);
	}



	@Override
	public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isEmpty(userTicket)) {
			return null;
		}
		User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
		if (user != null) {
			CookieUtil.setCookie(request, response, "userTicket", userTicket);
		}
		return user;
	}




}
