package pt.ulisboa.tecnico.socialsoftware.blcm;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pt.ulisboa.tecnico.socialsoftware.blcm.config.StartUpService;
@EnableJpaRepositories
@EnableTransactionManagement
@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
public class BusinessLogicConsistencyModelsApplication implements InitializingBean {

	@Autowired
	private StartUpService startUpService;

	public static void main(String[] args) {
		SpringApplication.run(BusinessLogicConsistencyModelsApplication.class, args);
	}

	@Override
	public void afterPropertiesSet() {
		// Run on startup
		startUpService.clearEvents();
	}

}
