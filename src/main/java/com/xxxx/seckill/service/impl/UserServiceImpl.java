package com.xxxx.seckill.service.impl;


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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务实现类
 * </p>
 * 乐字节：专注线上IT培训
 * 答疑老师微信：lezijie
 *
 * @author zhoubin
 */
@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 功能描述: 登录
	 *
	 * @param:
	 * @return: 乐字节：专注线上IT培训
	 * 答疑老师微信：lezijie
	 * @since: 1.0.0
	 * @Author: zhoubin
	 */
	@Override
	public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
		String mobile = loginVo.getMobile();
		String password = loginVo.getPassword();
		// //参数校验
		// if (StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
		// 	return RespBean.error(RespBeanEnum.LOGIN_ERROR);
		// }
		// if (!ValidatorUtil.isMobile(mobile)){
		// 	return RespBean.error(RespBeanEnum.MOBILE_ERROR);
		// }
		//根据手机号获取用户
		User user = new User();
		try {
			user= userMapper.selectById(mobile);
		}catch (Exception e){
			e.printStackTrace();
		}

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
		try {
			redisTemplate.opsForValue().set("user:" + ticket, user);
		}catch (Exception e){
			e.printStackTrace();
		}

		// request.getSession().setAttribute(ticket,user);
		CookieUtil.setCookie(request, response, "userTicket", ticket);
		return RespBean.success(ticket);
	}


	/**
	 * 功能描述: 根据cookie获取用户
	 *
	 * @param:
	 * @return: 乐字节：专注线上IT培训
	 * 答疑老师微信：lezijie
	 * @since: 1.0.0
	 * @Author: zhoubin
	 */
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
