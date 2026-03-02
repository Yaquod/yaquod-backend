package com.yaquodorg.yaquod.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.yaquodorg.yaquod.filter.AuthenticationEntryPointFilter;
import com.yaquodorg.yaquod.filter.CustomAccessDeniedFilter;
import com.yaquodorg.yaquod.filter.JwtAuthenticationFilter;
import com.yaquodorg.yaquod.security.VehicleAuthenticationProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
  private final AuthenticationProvider authenticationProvider;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CustomAccessDeniedFilter accessDeniedFilter;
  private final AuthenticationEntryPointFilter authenticationEntryPoint;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
  private final VehicleAuthenticationProvider vehicleAuthenticationProvider;

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration,
      VehicleAuthenticationProvider vehicleAuthenticationProvider)
      throws Exception {

    // Get the default authentication manager
    AuthenticationManager defaultManager = authenticationConfiguration.getAuthenticationManager();

    // Create a provider manager with both user and vehicle providers
    return new ProviderManager(List.of(vehicleAuthenticationProvider), defaultManager);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            req ->
                req.requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs*/**")
                    .permitAll()
                    .requestMatchers("/api/admins/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/clients/**")
                    .hasRole("CLIENT")
                    .anyRequest()
                    .authenticated())
        .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exception ->
                exception
                    .accessDeniedHandler(accessDeniedFilter)
                    .authenticationEntryPoint(authenticationEntryPoint))
        .oauth2Login(oauth -> oauth.successHandler(oAuth2LoginSuccessHandler));

    return http.build();
  }

  @Bean
  public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOriginPattern("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
