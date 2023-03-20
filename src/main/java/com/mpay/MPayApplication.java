package com.mpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MPayApplication extends ServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MPayApplication.class, args);
	}

}
