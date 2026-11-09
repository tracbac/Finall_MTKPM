package com.library.digital_library;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DigitalLibraryApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void applicationServicesUseSpringSingletonScope() {
		assertSame(applicationContext.getBean(com.library.digital_library.service.BookService.class),
				applicationContext.getBean(com.library.digital_library.service.BookService.class));
	}

	@Test
	void homePageAndPublicBookCatalogAreAccessibleWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("index.html"));

		mockMvc.perform(get("/auth.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang=\"vi\">")));

		mockMvc.perform(get("/admin.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang=\"vi\">")));

		mockMvc.perform(get("/my-books.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("<html lang=\"vi\">")));

		mockMvc.perform(get("/api/books"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
