package net.knasmueller.pathfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PathFinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PathFinderApplication.class, args);
	}
}
