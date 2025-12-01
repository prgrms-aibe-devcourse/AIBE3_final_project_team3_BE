package triplestar.mixchat.global.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthorizationFilter JwtAuthorizationFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                // OPTIONS 요청 허용 (CORS Preflight)
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                // 인증 불필요
                                .requestMatchers(
                                        "/", "/swagger-ui/**","/v3/api-docs/**",
                                        "/api/*/auth/join", "/api/*/auth/login",
                                        "/api/*/auth/reissue", "/api/*/auth/logout").permitAll()
                                // 회원 조회는 인증 불필요
                                .requestMatchers(HttpMethod.GET, "/api/v1/members").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/members/*").permitAll()
                                // NOTE : 테스트용 AI Chat API 임시 허용
                                .requestMatchers("/api/v1/ai/temp/**").permitAll()
                                // ADMIN 권한 필요
                                .requestMatchers("/api/*/admin/**").hasRole("ADMIN")
                                // WEBSOCKET 요청 허용
                                .requestMatchers("/ws-stomp/**").permitAll()
                                // 나머지 모든 요청은 인증 필요
                                .requestMatchers("/**").authenticated()
                )
                // JWT 인증 필터 적용
                .addFilterBefore(JwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handling -> handling
                                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // REST API는 사용하지 않는 Security 기본 기능 비활성화
                .csrf(AbstractHttpConfigurer::disable) // csrf 보호기능 비활성화
                .cors(Customizer.withDefaults()) // CORS 설정 적용
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성화
                .logout(AbstractHttpConfigurer::disable) // 로그아웃 기능 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 관리 STATELESS
        ;
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://mixchat.yhcho.com",
                "https://api.mixchat.yhcho.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }



    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
