package pl.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .httpBasic()
          .and()
          .authorizeRequests()
          .antMatchers("/index.html", "/h2-console/**", "/api/register**", "/api/login**", "/", "/api/user",
            "/swagger-ui.html", "/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/security",
            "/webjars/**").permitAll()
          .antMatchers("/api/admin/**").hasRole("ADMIN")
          .anyRequest().hasRole("USER")
          .and()
          .formLogin().usernameParameter("email").passwordParameter("password").loginPage("/api/login")
          .and()
          .cors().disable()
          .csrf().disable()
          .headers().frameOptions().disable();
    }
}
