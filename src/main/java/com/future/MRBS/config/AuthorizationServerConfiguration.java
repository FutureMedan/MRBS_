package com.future.MRBS.config;

import com.future.MRBS.service.ServiceImpl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration @EnableAuthorizationServer public class AuthorizationServerConfiguration
    extends AuthorizationServerConfigurerAdapter {

    private static final String CLIENT_ID = "MRBS-Client";
    private static final String CLIENT_SECRET = "MRBS-S3cr3t";
    private static final String SCOPE_READ = "read";
    private static final String SCOPE_WRITE = "write";
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final int ACCESS_TOKEN_DURATION = 3600; // -1 for development purpose
    private static final int REFRESH_TOKEN_DURATION = -1;

    private final TokenStore tokenStore;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceImpl userDetailsService;

    @Autowired public AuthorizationServerConfiguration(AuthenticationManager authenticationManager,
        TokenStore tokenStore, PasswordEncoder passwordEncoder,
        UserServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenStore = tokenStore;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient(CLIENT_ID).secret(passwordEncoder.encode(CLIENT_SECRET))
            .authorizedGrantTypes(GRANT_TYPE_PASSWORD, GRANT_TYPE_REFRESH_TOKEN,
                GRANT_TYPE_CLIENT_CREDENTIALS).scopes(SCOPE_READ, SCOPE_WRITE)
            .accessTokenValiditySeconds(ACCESS_TOKEN_DURATION)
            .refreshTokenValiditySeconds(REFRESH_TOKEN_DURATION);
    }

    @Override public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.tokenStore(tokenStore).authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService);
    }
}
