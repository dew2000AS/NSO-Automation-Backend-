//package com.example.SPSProjectBackend;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class SpsProjectBackendApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(SpsProjectBackendApplication.class, args);
//	}
//
//}


package com.example.SPSProjectBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

//@SpringBootApplication
//public class EvProjectApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(EvProjectApplication.class, args);
////		System.out.println("Hello World");
//	}
//
//}

@SpringBootApplication
public class SpsProjectBackendApplication extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(SpsProjectBackendApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpsProjectBackendApplication.class, args);
	}
}