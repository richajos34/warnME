/* package com.berkeley.irms.warnme.services;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/public/**").permitAll() // Define public access URLs
                .anyRequest().authenticated() // All other URLs require authentication
                .and()
            .formLogin() // Configure form-based login
                .loginPage("/signin") // Custom sign-in page
                .defaultSuccessUrl("/dashboard") // Redirect after successful login
                .permitAll() // Allow access to the sign-in page
                .and()
            .logout()
                .logoutSuccessUrl("/signin") // Redirect after logout
                .permitAll(); // Allow access to the logout URL
    }

    // Other configurations, user details service, password encoder, etc.
}
 */