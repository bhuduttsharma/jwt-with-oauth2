package com.bhu.login.config.jwtAuth;

import java.time.Instant;
import java.util.Objects;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.bhu.login.config.UserSecurityConfig;
import com.bhu.login.repo.UserRepo;

import lombok.RequiredArgsConstructor;

/**
 * @author bhudutt
 */
@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
	
	private final UserRepo useruserInfoRepo;

	public String getUserName(Jwt jwtToken){
        return jwtToken.getSubject();
    }
	
	public boolean isTokenValid(Jwt jwtToken, UserDetails userDetails){
        final String userName = getUserName(jwtToken);
        boolean isTokenExpired = getIfTokenIsExpired(jwtToken);
        boolean isTokenUserSameAsDatabase = userName.equals(userDetails.getUsername());
        return !isTokenExpired  && isTokenUserSameAsDatabase;

    }
	
	private boolean getIfTokenIsExpired(Jwt jwtToken) {
        return Objects.requireNonNull(jwtToken.getExpiresAt()).isBefore(Instant.now());
    }
	
	
    public UserDetails userDetails(String emailId){
        return useruserInfoRepo
                .findByEmailId(emailId)
                .map(UserSecurityConfig::new)
                .orElseThrow(()-> new UsernameNotFoundException("UserEmail: "+emailId+" does not exist"));
    }
}
