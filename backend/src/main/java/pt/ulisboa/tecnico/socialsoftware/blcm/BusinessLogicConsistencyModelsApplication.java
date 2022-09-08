package pt.ulisboa.tecnico.socialsoftware.blcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusinessLogicConsistencyModelsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusinessLogicConsistencyModelsApplication.class, args);
	}

}
