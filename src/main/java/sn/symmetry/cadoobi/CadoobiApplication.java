package sn.symmetry.cadoobi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@SpringBootApplication(exclude = {
	org.springdoc.core.configuration.SpringDocDataRestConfiguration.class,
	org.springdoc.core.configuration.SpringDocHateoasConfiguration.class
})
public class CadoobiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CadoobiApplication.class, args);
	}

}
