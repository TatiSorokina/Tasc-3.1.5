package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.api.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13.5");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void createUserTest() throws Exception {

       UserRequest userRequest = new UserRequest("sena", "sena@gmail.com", "1234",
                "Sena", "Senina");

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userRequest)))
                        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserById() throws Exception {
        var userID = "7faac39c-2e5a-4bd6-9554-3b81bb340c25";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userID))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                    {
                                       "firstName": "Tati",
                                       "lastName": "Sorokina",
                                       "roles": [
                                         "default-roles-itm"
                                       ],
                                       "groups": [
                                         "Moderators"
                                       ]
                                     }
                                """)
                );
    }

    @Test
    @WithMockUser(username = "testUser", roles = "MODERATOR")
    void helloIntegrationTest() throws Exception {

        String expectedUsername = "testUser";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/hello")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertEquals(expectedUsername, responseContent);
    }

    private static String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
