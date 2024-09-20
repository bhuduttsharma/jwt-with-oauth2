package com.bhu.login.service;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import com.bhu.login.dto.TokenType;
import com.bhu.login.repo.RefreshTokenRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author bhudutt
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutHandlerService implements LogoutHandler {

	private final RefreshTokenRepo refreshTokenRepo;
	
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		// TODO Auto-generated method stub
		 final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		 if(!authHeader.startsWith(TokenType.Bearer.name())){
	            return;
	        }
		 final String refreshToken = authHeader.substring(7);
		 
		 var storedRefreshToken = refreshTokenRepo.findByRefreshToken(refreshToken)
	                .map(token->{
	                    token.setRevoked(true);
	                    refreshTokenRepo.save(token);
	                    return token;
	                })
	                .orElse(null);
		
	}

}
