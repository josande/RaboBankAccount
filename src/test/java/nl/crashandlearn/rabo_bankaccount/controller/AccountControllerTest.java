package nl.crashandlearn.rabo_bankaccount.controller;

import nl.crashandlearn.rabo_bankaccount.exception.GlobalExceptionHandler;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Mock
    private AccountService service;
    @Spy
    private AccountAssembler assembler;

    @InjectMocks
    private AccountController controller;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void getAccountsForUser() throws Exception {
        Account account = Account.builder().id(1L).balance(100.0d).build();
        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(account);
        given(service.getAllAccountsForUser()).willReturn(accounts);

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/account/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void getAccountById_exists() throws Exception {
        Account account = Account.builder().id(1L).balance(100.0d).build();
        given(service.findById(1L)).willReturn(Optional.of(account));

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/account/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void getAccountById_missing() throws Exception {
        given(service.findById(1L)).willReturn(Optional.empty());

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/account/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void getAllAccounts() throws Exception {
        Account account = Account.builder().id(1L).balance(100.0d).build();
        ArrayList<Account> accounts = new ArrayList<>();
        accounts.add(account);
        given(service.getAllAccounts()).willReturn(accounts);

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/account/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void testCreateAccount() throws Exception {
    Account account = Account.builder().id(1L).balance(100.0d).build();
    given(service.createAccount(100.0d)).willReturn(account);

    // when
        MockHttpServletResponse response = mvc.perform(
                        post("/account/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "balance": 100
                                        }""")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
    }

    @Test
    public void testDeleteAccount() throws Exception {

        // when
        MockHttpServletResponse response = mvc.perform(
                        delete("/account/1/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }


    @Test
    public void testWithdraw() throws Exception {
        // when
        MockHttpServletResponse response = mvc.perform(
                        put("/account/withdraw")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "accountIdFrom": 1,
                                          "amount": 10
                                        }""")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }

    @Test
    public void testTransfer() throws Exception {
        // when
        MockHttpServletResponse response = mvc.perform(
                        put("/account/withdraw")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "accountIdFrom": 1,
                                          "accountIdTo": 2,
                                          "amount": 10
                                        }""")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
}
