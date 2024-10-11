package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class MySecurityConfig {
	
	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
		}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
		
	}
	
	public DaoAuthenticationProvider authenticationProvider() {
DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
return daoAuthenticationProvider;

}
	
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorize -> authorize

                                .antMatchers("/admin/**").hasRole("ADMIN") // URLs starting with "/admin" require ADMIN role
                                .antMatchers("/user/**").hasRole("USER") // URLs starting with "/user" require USER role
                                
                                .anyRequest().permitAll() // Allow access to all other URLs
                )
                .formLogin(form -> form
                        .loginPage("/signin") // Customize login page URL
                        .loginProcessingUrl("/dologin")
                        .defaultSuccessUrl("/user/index")
                    )
                .logout(withDefaults())
                .csrf(csrf -> csrf.disable());// Use default form login
        

     return http.build();
	}
	
	
	
}
