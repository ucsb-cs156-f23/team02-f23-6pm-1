package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRequestRepository requestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/RecomendationRequest/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_requests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2023-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-03-11T00:00:00");

                RecommendationRequest req1 = RecommendationRequest.builder()
                                        .requesterEmail("apchau@ucsb.edu")
                                        .professorEmail("pconrad@ucsb.edu")
                                        .explanation("for grad school")
                                        .dateRequested(ldt1)
                                        .dateNeeded(ldt2)
                                        .done(true)
                                        .build();

                LocalDateTime ldt3 = LocalDateTime.parse("2023-08-25T00:00:00");
                LocalDateTime ldt4 = LocalDateTime.parse("2024-05-30T00:00:00");

                RecommendationRequest req2 = RecommendationRequest.builder()
                                        .requesterEmail("dogcat@ucsb.edu")
                                        .professorEmail("mmouse@ucsb.edu")
                                        .explanation("for phd program")
                                        .dateRequested(ldt3)
                                        .dateNeeded(ldt4)
                                        .done(true)
                                        .build();

                ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
                expectedRequests.addAll(Arrays.asList(req1, req2));

                when(requestRepository.findAll()).thenReturn(expectedRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(requestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRequests);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbdates/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_request() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2023-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-03-11T00:00:00");

                RecommendationRequest req1 = RecommendationRequest.builder()
                                        .requesterEmail("apchau@ucsb.edu")
                                        .professorEmail("pconrad@ucsb.edu")
                                        .explanation("for grad school")
                                        .dateRequested(ldt1)
                                        .dateNeeded(ldt2)
                                        .done(true)
                                        .build();

                when(requestRepository.save(eq(req1))).thenReturn(req1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/RecommendationRequest/post?requesterEmail=apchau@ucsb.edu&professorEmail=pconrad@ucsb.edu&explanation=for grad school&dateRequested=2023-01-03T00:00:00&dateNeeded=2023-03-11T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(requestRepository, times(1)).save(req1);
                String expectedJson = mapper.writeValueAsString(req1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/RecommendationRequest?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2023-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-03-11T00:00:00");

                RecommendationRequest req1 = RecommendationRequest.builder()
                                        .requesterEmail("apchau@ucsb.edu")
                                        .professorEmail("pconrad@ucsb.edu")
                                        .explanation("for grad school")
                                        .dateRequested(ldt1)
                                        .dateNeeded(ldt2)
                                        .done(true)
                                        .build();

                when(requestRepository.findById(eq(7L))).thenReturn(Optional.of(req1));

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(requestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(req1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(requestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(requestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
        }
}
