package nl.crashandlearn.rabo_bankaccount.service;

import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith({MockitoExtension.class})
@SpringBootTest
class UserServiceTest {

    @Mock
    private UserRepository repository;
    @Mock
    private AuthenticationHelperService authHelper;

    @Test
    void returnZeroIsNoValues() {
        UserService userService = new UserService(repository, authHelper);
        when(repository.getBalance(any())).thenReturn(null);
        assertEquals(0, userService.getBalance());
    }
}