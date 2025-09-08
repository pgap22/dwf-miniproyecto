package sv.edu.udb.data_collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DataCollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataCollectorApplication.class, args);
	}

}
