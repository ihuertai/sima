package mx.ipn.sima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SimaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimaApplication.class, args);
	}

}
