package ba.maloprodaja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@EnableScheduling
public class MaloprodajaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaloprodajaApplication.class, args);
    }
}
