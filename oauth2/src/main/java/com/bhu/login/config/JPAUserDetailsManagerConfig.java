package com.bhu.login.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bhu.login.repo.UserRepo;

import lombok.RequiredArgsConstructor;


/**
 * bhudutt
 */
@Service
@RequiredArgsConstructor
public class JPAUserDetailsManagerConfig implements UserDetailsService {
	
	private final UserRepo userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		 return userRepo
				 .findByEmailId(emailId)
	                .map(UserSecurityConfig::new)
	                .orElseThrow(()-> new UsernameNotFoundException("User: "+emailId+" does not exist"));
	}

}
