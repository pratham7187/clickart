package com.clickkart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClickKart Backend — Spring Boot Application Entry Point
 *
 * <p>This is the single class that starts the entire application.
 * {@code @SpringBootApplication} is a meta-annotation that enables:
 *
 * <ul>
 *   <li>{@code @Configuration}      — marks this as a Spring configuration class</li>
 *   <li>{@code @EnableAutoConfiguration} — activates Spring Boot's auto-configuration
 *       (e.g., auto-creates DataSource from application.properties, auto-configures
 *       JPA, sets up embedded Tomcat, etc.)</li>
 *   <li>{@code @ComponentScan}      — scans the {@code com.clickkart} package and all
 *       sub-packages for Spring beans (@Component, @Service, @Repository, @Controller,
 *       @RestController, @Configuration)</li>
 * </ul>
 *
 * <p><b>How to run:</b>
 * <pre>
 *   # From IntelliJ: click the green ▶ button on this class
 *   # From terminal: mvn spring-boot:run
 *   # From JAR:      java -jar target/clickkart-backend-1.0.0.jar
 *   # With profile:  java -jar target/clickkart-backend-1.0.0.jar --spring.profiles.active=dev
 * </pre>
 *
 * <p><b>Startup checklist:</b>
 * <ol>
 *   <li>MySQL 8 is running on localhost:3306</li>
 *   <li>Database {@code clickkart_db} exists (run {@code clickkart_schema.sql} first)</li>
 *   <li>{@code application.properties} has the correct DB username/password</li>
 * </ol>
 */
@SpringBootApplication
public class ClickkartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClickkartApplication.class, args);
    }

}
