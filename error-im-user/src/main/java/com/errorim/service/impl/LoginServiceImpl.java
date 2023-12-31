package com.errorim.service.impl;


import com.errorim.dto.LoginDTO;
import com.errorim.entity.ResponseResult;
import com.errorim.entity.User;
import com.errorim.exception.ErrorImException;
import com.errorim.security.EmailCodeAuthenticationToken;
import com.errorim.service.LoginService;
import com.errorim.util.JwtUtil;
import com.errorim.util.RedisCache;
import com.errorim.vo.LoginVO;
import io.jsonwebtoken.Claims;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {


	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private RedisCache redisCache;

	@Override
	public ResponseResult login(LoginDTO loginDTO) {
//		UsernamePasswordAuthenticationToken authentication =
//				new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
		EmailCodeAuthenticationToken authentication =
				new EmailCodeAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());


		Authentication authenticate = authenticationManager.authenticate(authentication);

		if (Objects.isNull(authenticate)) {
			throw new ErrorImException(HttpStatus.UNAUTHORIZED.value(), "登录失败");
		}

		//使用UUID 作为存入Redis的key，防止用户退出后，旧的token还可以继续使用

		String uuid = UUID.randomUUID().toString();

		String jwt = JwtUtil.createJWT(uuid);
		LoginVO loginVO = new LoginVO();
		loginVO.setToken(jwt);

		redisCache.setCacheObject(uuid, authenticate.getPrincipal(), 1, TimeUnit.DAYS);

		return ResponseResult.okResult(loginVO);
	}

	@Override
	public ResponseResult logout(String token) {
		String uuid;
		try {
			Claims claims = JwtUtil.parseJWT(token);
			uuid = claims.getSubject();
		} catch (Exception e) {
			throw new RuntimeException("退出失败");
		}

		redisCache.deleteObject(uuid);

		return ResponseResult.okResult();
	}
}
