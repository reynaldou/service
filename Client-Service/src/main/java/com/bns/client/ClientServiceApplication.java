package com.bns.client;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableCircuitBreaker
@EnableBinding(Source.class )
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ClientServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientServiceApplication.class, args);
	}
}


@RestController
@RequestMapping("/services")
class ClientGateway{

	@Autowired
	private RestTemplate restTemplate;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public Collection<String> getServiceNameFallBack(){

        return Arrays.asList("Try later!, Service is down...");
    }

    @Autowired
    private Source source;

    @RequestMapping(method = RequestMethod.POST)
    public void writeService(@RequestBody Service service){

        Message<String> message = MessageBuilder.withPayload(service.getServiceName()).build();

        this.source.output().send(message);

    }


    @HystrixCommand(fallbackMethod = "getServiceNameFallBack")
    @RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> getServiceName(){

		ParameterizedTypeReference<Resources<Service>> ptr = new ParameterizedTypeReference<Resources<Service>>() {};

		ResponseEntity<Resources<Service>> entity =  this.restTemplate.exchange("http://service/services", HttpMethod.GET, null, ptr );

		return entity.getBody().getContent().stream().map(Service::getServiceName).collect(Collectors.toList());

	}
}

class Service{

	private String serviceName;

	public String getServiceName() {
		return serviceName;
	}

}