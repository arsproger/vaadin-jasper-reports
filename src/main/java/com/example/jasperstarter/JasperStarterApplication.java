package com.example.jasperstarter;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@PWA(name = "Vaadin Jasper Report Ui App", shortName = "Report Ui app")
public class JasperStarterApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(JasperStarterApplication.class, args);
	}

}
