package nl.crashandlearn.rabo_bankaccount;

import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;


@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Simple Bank account API",
        version = "1.0",
        description = "Handles users, customers and accounts"),
        security = {@SecurityRequirement(name = "BearerAuthentication") })
public class RaboBankAccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(RaboBankAccountApplication.class, args);
    }

}
