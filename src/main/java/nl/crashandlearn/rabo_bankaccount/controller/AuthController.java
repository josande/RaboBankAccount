package nl.crashandlearn.rabo_bankaccount.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import nl.crashandlearn.rabo_bankaccount.model.Role;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import nl.crashandlearn.rabo_bankaccount.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtils jwtUtils;


    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtils = jwtUtils;
    }

    private record LoginBody(
            @NotBlank
            @Schema(description = "Username to log in with", example = "username")
            String username,
            @NotBlank
            @Schema(description = "Your password.", example = "12345")
            String password) {}

    private record AuthResponse(String accessToken) {}

    @Operation(summary = "Log in with username and password")
    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginBody body) {
        Authentication authentication =
                authenticationManager
                        .authenticate(
                                new UsernamePasswordAuthenticationToken(body.username, body.password));
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        String accessToken = jwtUtils.generateJwtToken(authentication);

        return new ResponseEntity<>(new AuthResponse(accessToken), HttpStatus.OK);
    }

    private record RegisterUserBody(
            @NotBlank
            @Schema(description = "Username to log in with", example = "username")
            String username,
            @NotBlank
            @Size(min = 5)
            @Schema(description = "At least 5 characters.", example = "12345")
            String password) {}

    @Operation(summary = "Register a new user")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User registered")})
    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterUserBody body) {

        if(userRepository.existsByUsername(body.username))
            return ResponseEntity.badRequest().body("Username already taken.");

        User user = User.builder()
                .username(body.username)
                .password(passwordEncoder.encode((body.password)))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }
    @Operation(summary = "List all users")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Users")})
    @GetMapping("")
    ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

}