package nl.crashandlearn.rabo_bankaccount.controller;

import jakarta.annotation.Resource;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.model.CardType;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.service.AccountService;
import nl.crashandlearn.rabo_bankaccount.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
class AccountE2ETests {

    @Resource
    UserController userController;

    User user = User.builder().username("user").password("pass").build();
    User admin = User.builder().username("admin").password("pass").build();

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AuthController authController;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CardController cardController;
    @Autowired
    private UserService userService;

    private void setCurrentUser(User user) {
        Authentication authentication = authenticationManager .authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    void beforeEach() {
        authController.registerUser(new AuthController.RegisterUserDto(user.getUsername(), user.getPassword(), "", "", ""));
        authController.registerAdmin(new AuthController.RegisterUserDto(admin.getUsername(), admin.getPassword(), "", "", ""));
    }

    @Test
    public void test_getBalanceForCurrentUser() {
        setCurrentUser(user);

        accountService.createAccount(100.0d);
        accountService.createAccount(75.0d);

        var response = userController.getCurrentUserBalance();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(175.0d, response.getBody());
    }

    @Test
    public void test_getUserDetailsAsAdmin() {
        setCurrentUser(user);
        long userId = userService.getCurrentUser().getId();
        Account a1 = accountService.createAccount(100.0d);
        accountService.createAccount(75.0d);
        cardController.createCard(new CardController.NewCardDto(a1.getId(), CardType.CREDIT_CARD));

        setCurrentUser(admin);
        var response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userId, response.getBody().getId());
        assertEquals("user", response.getBody().getUsername());
    }


}
