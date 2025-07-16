package com.microservice.product_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.product_service.dto.ProductRequest;
import com.microservice.product_service.repository.ProductRepository;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	ProductRepository productRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.data.mongdb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@BeforeEach
	void setup() {
		productRepository.deleteAll();
	}

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productReqeustString = objectMapper.writeValueAsString(productRequest);
		createProduct(productReqeustString);
		Assertions.assertEquals(1, productRepository.findAll().size());
	}


	@Test
	void shouldGetProductRequest() throws Exception {
	 List<ProductRequest> productRequests = getAllProducts();

	 List<String> insertProducts = productRequests.stream()
			 .map(productRequest -> {
                 try {
                     return objectMapper.writeValueAsString(productRequest);
                 } catch (JsonProcessingException e) {
                     throw new RuntimeException(e);
                 }
             }).toList();

		for(String productRequest : insertProducts){
			createProduct(productRequest);
		}

	 mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
			 .andExpect(status().isOk());
		Assertions.assertEquals(2, productRepository.findAll().size());
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("Test Product")
				.description("Test Description")
				.price(BigDecimal.valueOf(20.00))
				.build();
	}

	private List<ProductRequest> getAllProducts() {
		List<ProductRequest> productRequests = new ArrayList<>();
		productRequests.add(ProductRequest.builder()
				.name("Test Product 1")
				.description("test description 1")
				.price(BigDecimal.valueOf(1))
				.build());
		productRequests.add(ProductRequest.builder()
				.name("Test Product 2")
				.description("test description 2")
				.price(BigDecimal.valueOf(2))
				.build());

		return productRequests;
	}

	private void createProduct(String productReqeustString) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(String.valueOf(MediaType.APPLICATION_JSON))
						.content(productReqeustString))
				.andExpect(status().isCreated());
	}

}
