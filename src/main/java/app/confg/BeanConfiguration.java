package app.confg;

import app.security.OAuth2UserPrincipalResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class BeanConfiguration implements WebMvcConfigurer {

    private final OAuth2UserPrincipalResolver oAuth2UserPrincipalResolver;

    public BeanConfiguration(OAuth2UserPrincipalResolver oAuth2UserPrincipalResolver) {
        this.oAuth2UserPrincipalResolver = oAuth2UserPrincipalResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(oAuth2UserPrincipalResolver);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
