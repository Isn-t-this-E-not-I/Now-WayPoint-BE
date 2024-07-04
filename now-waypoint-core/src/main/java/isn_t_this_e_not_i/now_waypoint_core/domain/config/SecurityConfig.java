package isn_t_this_e_not_i.now_waypoint_core.domain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtFilter;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtLoginFilter;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtUtil;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.handler.OAuth2SuccessHandler;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.service.OAuth2UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserDetailService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final ObjectMapper objectMapper;
    private final UserDetailService userDetailService;
    private final LogoutHandler logoutService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserService userService() {
        return new UserService(userRepository, bCryptPasswordEncoder(), tokenService);
    }

    @Bean
    public OAuth2UserService oAuth2UserService(){
        return new OAuth2UserService(userService());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtLoginFilter jwtLoginFilter = new JwtLoginFilter(authenticationManager(authenticationConfiguration),jwtUtil, objectMapper,tokenService);
        jwtLoginFilter.setFilterProcessesUrl("/api/user/login");
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, userDetailService, tokenService, objectMapper);
        //security 경로설정
        http
                .csrf(auth -> auth.disable())
                .formLogin(auth -> auth.disable())
                .httpBasic(auth -> auth.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/api/user/login", "/api/user/register","/main").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(login -> login
                        .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/user/login"))
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(oAuth2UserService()))
                        .successHandler(oAuth2SuccessHandler))
                .addFilterAt(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtFilter, OAuth2LoginAuthenticationFilter.class)
                .addFilterAfter(jwtLoginFilter, JwtFilter.class)
                .logout(logoutConf -> logoutConf
                        .logoutUrl("/api/user/logout")
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler((req,res,auth) ->
                                SecurityContextHolder.clearContext()));

        //securityCors설정
        http
                .cors(cors -> cors
                        .configurationSource(new CorsConfigurationSource() {
                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                                CorsConfiguration corsConfiguration = new CorsConfiguration();

                                corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                                corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
                                corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
                                corsConfiguration.setAllowCredentials(true);
                                corsConfiguration.setMaxAge(3000L);
                                corsConfiguration.setExposedHeaders(Collections.singletonList("Authorization"));

                                return corsConfiguration;
                            }
                        }));

        return http.build();
    }
}
