package mgmt.student.studentapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class AppConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin_user = User.builder()
                .username("wasilwam")
                .password(passwordEncoder().encode("wasilwam@1"))
                .roles("ADMIN")
                .build();
//        UserDetails normal_user = User.builder()
//                .username("mark")
//                .password(passwordEncoder().encode("wasilwam@2"))
//                .roles("USER")
//                .build();
        return new InMemoryUserDetailsManager(admin_user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static final String[] AUTH_WHITELIST = {
            "/auth/signin"
    };
}
