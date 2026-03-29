package sn.symmetry.cadoobi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
	org.springdoc.core.configuration.SpringDocDataRestConfiguration.class,
	org.springdoc.core.configuration.SpringDocHateoasConfiguration.class
})
public class CadoobiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CadoobiApplication.class, args);
	}

}
