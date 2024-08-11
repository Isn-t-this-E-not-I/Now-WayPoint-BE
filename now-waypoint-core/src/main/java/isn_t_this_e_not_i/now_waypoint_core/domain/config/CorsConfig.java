package isn_t_this_e_not_i.now_waypoint_core.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("Set-Cookie")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("http://localhost:3000","https://now-waypoint.com")
                .allowCredentials(true);
    }
}
