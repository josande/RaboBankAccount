package nl.crashandlearn.rabo_bankaccount.controller;

import jakarta.annotation.Resource;
import nl.crashandlearn.rabo_bankaccount.model.*;
import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import nl.crashandlearn.rabo_bankaccount.service.AccountService;
import nl.crashandlearn.rabo_bankaccount.service.CardService;
import nl.crashandlearn.rabo_bankaccount.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class AuditTests {


    @Resource
    UserController userController;
    @Resource
    AccountController accountController;
    @Resource
    AuditController auditController;

    @Autowired
    private UserRepository userRepository;

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
    private CardService cardService;
    @Autowired
    private UserService userService;

    private void setCurrentUser(User user) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @BeforeEach
    void beforeEach() {
        authController.registerUser(new AuthController.RegisterUserDto(user.getUsername(), user.getPassword(), "", "", ""));
        authController.registerAdmin(new AuthController.RegisterUserDto(admin.getUsername(), admin.getPassword(), "", "", ""));
    }

    @Test
    public void test_UserCanNotSeeAudit() {
        setCurrentUser(user);
        long userId = userService.getCurrentUser().getId();
        assertThrows(AccessDeniedException.class, () -> auditController.getAllPosts());
        assertThrows(AccessDeniedException.class, () -> auditController.getAllPostsForUser(userId));
    }

    @Test
    public void test_PaymentsAndWithdrawalsFromAccountLogged() {
        setCurrentUser(user);

        Account a1 = accountService.createAccount(100.0d);
        Account a2 = accountService.createAccount(75.0d);
        accountService.accountTransfer(50, a1.getId(), a2.getId());
        accountService.accountWithdrawal(25, a2.getId());

        setCurrentUser(admin);

        var posts = auditController.getAllPosts();

        assertEquals(2, posts.getContent().size());
    }

    @Test
    public void test_PaymentsAndWithdrawalsFromCardLogged() {
        setCurrentUser(user);
        long userId = userService.getCurrentUser().getId();

        Account a1 = accountService.createAccount(100.0d);
        Account a2 = accountService.createAccount(75.0d);
        Card creditCard = cardService.createCard(a1.getId(), CardType.CREDIT_CARD);
        Card debitCard = cardService.createCard(a2.getId(), CardType.DEBIT_CARD);
        cardController.withdraw(new CardController.CardWithdrawDto(creditCard.getId(), 55.0));
        cardController.withdraw(new CardController.CardWithdrawDto(debitCard.getId(), 55.0));
        cardController.transfer(new CardController.CardTransferDto(creditCard.getId(), a2.getId(), 25.0));
        cardController.transfer(new CardController.CardTransferDto(debitCard.getId(), a1.getId(), 25.0));

        setCurrentUser(admin);

        var posts = auditController.getAllPostsForUser(userId);

        assertEquals(4, posts.getContent().size());
    }

    @Test
    public void test_AuditShowsActorNotOwner() {
        setCurrentUser(user);
        long userId = userService.getCurrentUser().getId();

        Account a1 = accountService.createAccount(100.0d);
        Account a2 = accountService.createAccount(75.0d);

        accountService.accountTransfer(50, a1.getId(), a2.getId());
        accountService.accountTransfer(10, a1.getId(), a2.getId());

        setCurrentUser(admin);
        long adminId = userService.getCurrentUser().getId();
        accountService.accountWithdrawal(25, a2.getId());

        var userAudit = auditController.getAllPostsForUser(userId);
        var adminAudit = auditController.getAllPostsForUser(adminId);

        assertEquals(2, userAudit.getContent().size());
        assertEquals(1, adminAudit.getContent().size());
    }
}
