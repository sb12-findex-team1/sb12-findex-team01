package com.codeit.findex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class Sb12FindexTeam1Application {

  public static void main(String[] args) {
    SpringApplication.run(Sb12FindexTeam1Application.class, args);
  }

}
