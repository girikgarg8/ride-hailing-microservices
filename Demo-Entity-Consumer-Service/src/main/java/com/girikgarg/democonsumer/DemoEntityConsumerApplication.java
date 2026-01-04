package com.girikgarg.democonsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = "com.girikgarg.uberentityservice.models")  // ← Scan entities from imported package
public class DemoEntityConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoEntityConsumerApplication.class, args);
	}

}

