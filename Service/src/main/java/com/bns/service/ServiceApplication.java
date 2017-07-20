package com.bns.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@EnableDiscoveryClient
@IntegrationComponentScan
@EnableBinding(Sink.class)
@SpringBootApplication
public class ServiceApplication {

	@Bean
	CommandLineRunner commandLineRunner (ServiceRepository serviceRepository){
		return String -> {
            Stream.of("One", "Two", "Three").forEach(name -> serviceRepository.save(new Service(name)));
		};
	}

	public static void main(String... args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}

@MessageEndpoint
class ServiceProcessor{

    @Autowired
    private ServiceRepository serviceRepository;


    @ServiceActivator(inputChannel = Sink.INPUT)
    public void acceptNewServices(String serviceName){
        this.serviceRepository.save(new Service(serviceName));
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
interface ServiceRepository extends JpaRepository<Service, Long> {
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

    public Long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", Service Name:'" + serviceName + '\'' +
                '}';
    }

}