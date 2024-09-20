package com.bhu.login;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bhu.login.config.RSAKeyRecord;
import com.bhu.login.entity.UserEntity;
import com.bhu.login.repo.UserRepo;

@SpringBootApplication
//@EnableDiscoveryClient
@EnableConfigurationProperties(RSAKeyRecord.class)
public class LoginServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginServiceApplication.class, args);
		System.out.println("login service started....");
	}
	
	 
    
}
