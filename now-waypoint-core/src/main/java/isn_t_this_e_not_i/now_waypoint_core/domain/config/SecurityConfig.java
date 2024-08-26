package isn_t_this_e_not_i.now_waypoint_core.domain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtFilter;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtLoginFilter;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtUtil;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.handler.OAuth2FailureHandler;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.handler.OAuth2SuccessHandler;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.service.OAuth2UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserDetailService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserFollowService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.service.ChatMessageService;
import isn_t_this_e_not_i.now_waypoint_core.domain.chat.service.UserChatRoomService;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.FileUploadService;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
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
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final PostService postService;
    private final FileUploadService fileUploadService;
    private final UserFollowService userFollowService;
    private final ChatMessageService chatMessageService;
    private final UserChatRoomService userChatRoomService;

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
        return new UserService(postService, userRepository, bCryptPasswordEncoder(), tokenService,fileUploadService, userFollowService, chatMessageService, userChatRoomService);
    }

    @Bean
    public OAuth2UserService oAuth2UserService() {
        return new OAuth2UserService(userService());
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);  // Allow URL encoded slash
        firewall.setAllowSemicolon(true);  // Allow semicolon in URL
        firewall.setAllowBackSlash(true);  // Allow backslash in URL
        firewall.setAllowUrlEncodedDoubleSlash(true); // Allow double slash in URL
        return firewall;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtLoginFilter jwtLoginFilter = new JwtLoginFilter(authenticationManager(authenticationConfiguration)
                , jwtUtil, objectMapper, tokenService,userRepository);
        jwtLoginFilter.setFilterProcessesUrl("/api/user/login");
        JwtFilter jwtFilter = new JwtFilter(jwtUtil, userDetailService, tokenService, userRepository);

        // Security configuration
        http
                .csrf(auth -> auth.disable())
                .formLogin(auth -> auth.disable())
                .httpBasic(auth -> auth.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/user/login","/api/user/register","/api/user/userId","/api/user/password/find","/api/mail/**", "/api/user/checkLoginId", "/api/follow/loginInfo").permitAll()
                        .requestMatchers("/favicon.ico","/api/user/login/kakao", "/login/oauth2/code/kakao","/error","/oauth2/redirect" , "/api/user/login/google","/login/oauth2/code/google", "/api/user/login/naver","/login/oauth2/code/naver").permitAll()
                        .requestMatchers("/ws/**","/main/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger 경로 추가
                        .anyRequest().authenticated())
                .exceptionHandling(handler -> handler.authenticationEntryPoint(oAuth2FailureHandler))
                .oauth2Login(login -> login
                        .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/user/login"))
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(oAuth2UserService()))
                        .successHandler(oAuth2SuccessHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logoutConf -> logoutConf
                        .logoutUrl("/api/user/logout")
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler((req, res, auth) -> SecurityContextHolder.clearContext()));

        // CORS configuration
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://goorm.now-waypoint.store"));
            corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
            corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
            corsConfiguration.setAllowCredentials(true);
            corsConfiguration.setMaxAge(3000L);
            corsConfiguration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie", "ETag"));

            return corsConfiguration;
        }));

        return http.build();
    }
}
