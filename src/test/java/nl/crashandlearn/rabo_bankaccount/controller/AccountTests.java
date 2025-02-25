package nl.crashandlearn.rabo_bankaccount.controller;

import nl.crashandlearn.rabo_bankaccount.model.Account;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
class AccountTests {

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
    @Autowired
    private AccountController accountController;

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
    public void test_canCreateAccountAsUser() {
        setCurrentUser(user);
        var response = accountController.createAccount(new AccountController.NewAccountDto(175.0));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(175.0, response.getBody().getContent().getBalance());
    }

    @Test
    public void test_getAccountsForUser() {
        setCurrentUser(user);
        accountService.createAccount(145.5);
        accountService.createAccount(121.1);
        var response = accountController.getAccountsForUser();

        assertEquals(2, response.getContent().size());
    }

    @Test
    public void test_getAccountByIdAsAdmin() {
        setCurrentUser(user);
        Long userId = userService.getCurrentUser().getId();
        Account a = accountService.createAccount(145.5);

        setCurrentUser(admin);
        var response = accountController.getAccountById(a.getId());

        assertEquals(145.5, response.getContent().getBalance());
        assertEquals(userId, response.getContent().getUser().getId());
    }

    @Test
    public void test_getAccountByIdAsUserIsDenied() {
        setCurrentUser(user);
        assertThrows(AccessDeniedException.class, () -> accountController.getAccountById(1L));
    }
    @Test
    public void test_getAllAccountsAsAdmin() {
        setCurrentUser(user);
        accountService.createAccount(111.1);
        accountService.createAccount(222.2);

        setCurrentUser(admin);
        var response = accountController.getAllAccounts();

        assertEquals(2, response.getContent().size());
    }

    @Test
    public void test_getAllAccountsAsUserIsDenied() {
        setCurrentUser(user);
        assertThrows(AccessDeniedException.class, () -> accountController.getAllAccounts());
    }

    @Test
    public void test_deleteAccountAsOwningUser() {
        setCurrentUser(user);
        Account a = accountService.createAccount(111.1);

        assertEquals(1, accountService.getAllAccounts().size());
        accountController.deleteAccount(a.getId());
        assertEquals(0, accountService.getAllAccounts().size());
    }
    @Test
    public void test_deleteAccountOrOtherAsUserIsDenied() {
        setCurrentUser(admin);
        Account a = accountService.createAccount(111.1);

        setCurrentUser(user);
        assertThrows(AccessDeniedException.class, () -> accountController.deleteAccount(a.getId()));
    }

    @Test
    public void test_deleteAccountForOtherAsAdmin() {
        setCurrentUser(user);
        Account a = accountService.createAccount(111.1);
        assertEquals(1, accountService.getAllAccounts().size());

        setCurrentUser(admin);
        accountController.deleteAccount(a.getId());

        setCurrentUser(user);
        assertEquals(0, accountService.getAllAccounts().size());
    }


}
