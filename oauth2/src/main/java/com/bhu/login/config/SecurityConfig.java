package com.bhu.login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.bhu.login.config.jwtAuth.JwtAccessTokenFilter;
import com.bhu.login.config.jwtAuth.JwtRefreshTokenFilter;
import com.bhu.login.config.jwtAuth.JwtTokenUtils;
import com.bhu.login.repo.RefreshTokenRepo;
import com.bhu.login.service.LogoutHandlerService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author bhudutt
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
	
	 private final JPAUserDetailsManagerConfig jpaUserDetailsManagerConfig;
	 @Autowired
	 private final RSAKeyRecord rsaKeyRecord;
	 private final JwtTokenUtils jwtTokenUtils;
	 private final RefreshTokenRepo refreshTokenRepo;
	 private final LogoutHandlerService logoutHandlerService;
	 
	//Multiple Configuration
	 @Order(1)
	    @Bean
	    public SecurityFilterChain signInSecurityFilterChain(HttpSecurity httpSecurity) throws Exception{
	        return httpSecurity
	                .securityMatcher(new AntPathRequestMatcher("/sign-in/**"))
	                .csrf(AbstractHttpConfigurer::disable)
	                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
	                .userDetailsService(jpaUserDetailsManagerConfig)
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .exceptionHandling(ex -> {
	                    ex.authenticationEntryPoint((request, response, authException) ->
	                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage()));
	                })
	                .httpBasic(withDefaults())
	                .build();
	    }
	 
	 @Order(2)
	    @Bean
	    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity httpSecurity) throws Exception{
	        return httpSecurity
	                .securityMatcher(new AntPathRequestMatcher("/api/**"))
	                .csrf(AbstractHttpConfigurer::disable)
	                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
	                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .addFilterBefore(new JwtAccessTokenFilter(rsaKeyRecord, jwtTokenUtils), UsernamePasswordAuthenticationFilter.class)
	                .exceptionHandling(ex -> {
	                    log.error("[SecurityConfig:apiSecurityFilterChain] Exception due to :{}",ex);
	                    ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint());
	                    ex.accessDeniedHandler(new BearerTokenAccessDeniedHandler());
	                })
	                .httpBasic(withDefaults())
	                .build();
	    }
	 
	 @Order(3)
	    @Bean
	    public SecurityFilterChain refreshTokenSecurityFilterChain(HttpSecurity httpSecurity) throws Exception{
	        return httpSecurity
	                .securityMatcher(new AntPathRequestMatcher("/refresh-token/**"))
	                .csrf(AbstractHttpConfigurer::disable)
	                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
	                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .addFilterBefore(new JwtRefreshTokenFilter(rsaKeyRecord,jwtTokenUtils,refreshTokenRepo), UsernamePasswordAuthenticationFilter.class)
	                .exceptionHandling(ex -> {
	                    log.error("[SecurityConfig:refreshTokenSecurityFilterChain] Exception due to :{}",ex);
	                    ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint());
	                    ex.accessDeniedHandler(new BearerTokenAccessDeniedHandler());
	                })
	                .httpBasic(withDefaults())
	                .build();
	    }
	 
	 @Order(4)
	    @Bean
	    public SecurityFilterChain logoutSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
	        return httpSecurity
	                .securityMatcher(new AntPathRequestMatcher("/logout/**"))
	                .csrf(AbstractHttpConfigurer::disable)
	                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
	                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .addFilterBefore(new JwtAccessTokenFilter(rsaKeyRecord,jwtTokenUtils), UsernamePasswordAuthenticationFilter.class)
	                .logout(logout -> logout
	                        .logoutUrl("/logout")
	                        .addLogoutHandler(logoutHandlerService)
	                        .logoutSuccessHandler(((request, response, authentication) -> SecurityContextHolder.clearContext()))
	                )
	                .exceptionHandling(ex -> {
	                    log.error("[SecurityConfig:logoutSecurityFilterChain] Exception due to :{}",ex);
	                    ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint());
	                    ex.accessDeniedHandler(new BearerTokenAccessDeniedHandler());
	                })
	                .build();
	    }
	 
	 
	 @Order(5)
	    @Bean
	    public SecurityFilterChain registerSecurityFilterChain(HttpSecurity httpSecurity) throws Exception{
	        return httpSecurity
	                .securityMatcher(new AntPathRequestMatcher("/sign-up/**"))
	                .csrf(AbstractHttpConfigurer::disable)
	                .authorizeHttpRequests(auth ->
	                        auth.anyRequest().permitAll())
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .build();
	    }

	    
	    
	    @Bean
	    @Order(6)
	    public SecurityFilterChain h2ConsoleSecurityFilterChainConfig(HttpSecurity httpSecurity) throws Exception {
	        return httpSecurity
	                .securityMatcher(new AntPathRequestMatcher(("/h2-console/**")))
	                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
	                .csrf(csrf -> csrf.ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")))
	                .headers(headers -> headers.frameOptions(withDefaults()).disable())
	                .build();
	    }

	

	@Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	 @Bean
	    JwtDecoder jwtDecoder(){
	        return NimbusJwtDecoder.withPublicKey(rsaKeyRecord.rsaPublicKey()).build();
	    }
	 
	 @Bean
	    JwtEncoder jwtEncoder(){
	        JWK jwk = new RSAKey.Builder(rsaKeyRecord.rsaPublicKey()).privateKey(rsaKeyRecord.rsaPrivateKey()).build();
	        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
	        return new NimbusJwtEncoder(jwkSource);
	    }
}
