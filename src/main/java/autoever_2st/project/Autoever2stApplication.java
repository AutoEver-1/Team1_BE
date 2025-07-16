package autoever_2st.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Autoever2stApplication {

    public static void main(String[] args) {
        SpringApplication.run(Autoever2stApplication.class, args);
    }

}
