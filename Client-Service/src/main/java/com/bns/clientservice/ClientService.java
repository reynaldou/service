package com.bns.clientservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@EnableDiscoveryClient
@SpringBootApplication
public class ClientService {

	@Bean
	CommandLineRunner commandLineRunner (Repository repository){
		return String -> {
            Stream.of("ServiceOne", "ServiceTwo", "ServiceThree").forEach(name -> repository.save(new Service(name)));
		};
	}

	public static void main(String... args) {
		SpringApplication.run(ClientService.class, args);
	}
}

@RefreshScope
@RestController
class MessageRestController{

    @Value("${message}")
    private String msg;

    @RequestMapping("/message")
    String message(){
        return this.msg;
    }
}


@RepositoryRestResource
interface Repository extends JpaRepository<Service, Long> {
    @RestResource(path = "by-name")
    Collection<Service> findByServiceName(@Param("serviceName") String serviceName);
}


@Entity
class Service {

    @Id
    @GeneratedValue
    private Long id;
    private String serviceName;

    public Service() { //JPA
    }

    public Service(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }

}


