package com.example.habittracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@EnableAsync
@SpringBootApplication
public class HabittrackerApplication {

	public static void main(String[] args) {
		try {
			Dotenv dotenv = Dotenv.load();
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
			});
		} catch (io.github.cdimascio.dotenv.DotenvException e) {
			System.err.println("Warning: .env file not found or could not be loaded. Please ensure .env is in the project root.");
		}
		SpringApplication.run(HabittrackerApplication.class, args);
	}

}
