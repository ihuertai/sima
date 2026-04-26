package mx.ipn.sima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SimaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimaApplication.class, args);
    }
}