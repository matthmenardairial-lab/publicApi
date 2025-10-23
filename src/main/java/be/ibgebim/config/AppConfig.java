package be.ibgebim.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"be.ibgebim.service"})
public class AppConfig {
}
