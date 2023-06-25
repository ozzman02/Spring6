package com.ossant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ossant.model.CustomerDTO;
import com.ossant.services.CustomerService;
import com.ossant.services.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static com.ossant.controller.CustomerController.CUSTOMER_PATH;
import static com.ossant.controller.CustomerController.CUSTOMER_PATH_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CustomerService customerService;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<CustomerDTO> customerArgumentCaptor;

    CustomerServiceImpl customerServiceImpl;

    @BeforeEach
    void setup() {
        customerServiceImpl = new CustomerServiceImpl();
    }

    @Test
    void testListAllCustomers() throws Exception {
        given(customerService.getAllCustomers()).willReturn(customerServiceImpl.getAllCustomers());
        mockMvc.perform(get(CUSTOMER_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void testEmptyCustomerList() throws Exception {
        given(customerService.getAllCustomers()).willReturn(List.of());
        mockMvc.perform(get(CUSTOMER_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(0)));

    }
    @Test
    void testGetCustomerById() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        given(customerService.getCustomerById(customerDTO.getId())).willReturn(Optional.of(customerDTO));
        mockMvc.perform(get(CUSTOMER_PATH_ID, customerDTO.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(customerDTO.getId().toString())))
                .andExpect(jsonPath("$.name", is(customerDTO.getName())));
    }

    @Test
    void getCustomerByIdNotFound() throws Exception {
        //given(customerService.getCustomerById(any(UUID.class))).willThrow(NotFoundException.class);
        given(customerService.getCustomerById(any(UUID.class))).willReturn(Optional.empty());
        mockMvc.perform(get(CUSTOMER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveNewCustomer() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        customerDTO.setId(null);
        customerDTO.setVersion(null);
        given(customerService.saveNewCustomer(any(CustomerDTO.class)))
                .willReturn(customerServiceImpl.getAllCustomers().get(1));
        mockMvc.perform(post(CUSTOMER_PATH)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testUpdateCustomerByID() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        given(customerService.updateCustomerById(any(), any())).willReturn(Optional.of(customerDTO));
        mockMvc.perform(put(CUSTOMER_PATH_ID, customerDTO.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isNoContent());
        verify(customerService).updateCustomerById(any(UUID.class), any(CustomerDTO.class));
    }

    @Test
    void testUpdateCustomerByIdNotFound() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        given(customerService.updateCustomerById(any(), any())).willReturn(Optional.empty());
        mockMvc.perform(put(CUSTOMER_PATH_ID, customerDTO.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerDTO)))
                .andExpect(status().isNotFound());
        verify(customerService).updateCustomerById(any(UUID.class), any(CustomerDTO.class));
    }

    @Test
    void testDeleteCustomerById() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        given(customerService.deleteCustomerById(any())).willReturn(true);
        mockMvc.perform(delete(CUSTOMER_PATH_ID, customerDTO.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        //ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(customerService).deleteCustomerById(uuidArgumentCaptor.capture());
        assertThat(customerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void testDeleteCustomerByIdNotFound() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        given(customerService.deleteCustomerById(any())).willReturn(false);
        mockMvc.perform(delete(CUSTOMER_PATH_ID, customerDTO.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(customerService).deleteCustomerById(uuidArgumentCaptor.capture());
        assertThat(customerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }


    @Test
    void testPatchCustomerById() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("name", "New Name");
        given(customerService.patchCustomerById(any(), any())).willReturn(true);
        mockMvc.perform(patch(CUSTOMER_PATH_ID, customerDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerMap)))
                .andExpect(status().isNoContent());

        verify(customerService).patchCustomerById(uuidArgumentCaptor.capture(), customerArgumentCaptor.capture());

        assertThat(customerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(customerMap.get("name")).isEqualTo(customerArgumentCaptor.getValue().getName());
    }

    @Test
    void testPatchCustomerByIdNotFound() throws Exception {
        CustomerDTO customerDTO = customerServiceImpl.getAllCustomers().get(0);
        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("name", "New Name");
        given(customerService.patchCustomerById(any(), any())).willReturn(false);
        mockMvc.perform(patch(CUSTOMER_PATH_ID, customerDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerMap)))
                .andExpect(status().isNotFound());

        verify(customerService).patchCustomerById(uuidArgumentCaptor.capture(), customerArgumentCaptor.capture());

        assertThat(customerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(customerMap.get("name")).isEqualTo(customerArgumentCaptor.getValue().getName());
    }



}