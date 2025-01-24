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
                .username("admin")
                .password(passwordEncoder().encode("wasilwam@1"))
                .roles("ADMIN")
                .build();
        UserDetails student_maker = User.builder()
                .username("student_maker")
                .password(passwordEncoder().encode("maker@2"))
                .roles("STUDENT_MAKER")
                .build();
        UserDetails student_checker = User.builder()
                .username("student_checker")
                .password(passwordEncoder().encode("checker@2"))
                .roles("STUDENT_CHECKER")
                .build();
        return new InMemoryUserDetailsManager(admin_user, student_maker, student_checker);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static final String[] AUTH_WHITELIST = {
            "/auth/signin",
            "/h2-console/**",
            "/students/file/**"
    };
}
