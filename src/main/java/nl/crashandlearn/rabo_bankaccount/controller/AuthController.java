package nl.crashandlearn.rabo_bankaccount.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import nl.crashandlearn.rabo_bankaccount.exception.ErrorDto;
import nl.crashandlearn.rabo_bankaccount.model.Role;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.repository.UserRepository;
import nl.crashandlearn.rabo_bankaccount.security.JwtUtils;
import nl.crashandlearn.rabo_bankaccount.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@SecurityRequirements()
public class AuthController {

    private AuthService authService;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtUtils jwtUtils;


    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          AuthService authService,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }

    private record LoginDto(
            @NotBlank
            @Schema(description = "Username to log in with", example = "username")
            String username,
            @NotBlank
            @Schema(description = "Your password.", example = "12345")
            String password) {}

    private record AuthResponse(String accessToken) {}

    @Operation(summary = "Log in with username and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }) })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginDto body) {
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

    private record RegisterUserDto(
            @NotBlank
            @Schema(description = "Username to log in with", example = "username")
            String username,
            @NotBlank
            @Size(min = 5)
            @Schema(description = "At least 5 characters.", example = "12345")
            String password,
            @Schema(example = "Jane")
            String firstName,
            @Schema(example = "Doe")
            String lastName,
            @Email
            @Schema(example = "jane.doe@email.com")
            String email
            ) {}

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }) })

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid AuthController.RegisterUserDto body) {

        User user = User.builder()
                .username(body.username)
                .password(passwordEncoder.encode((body.password)))
                .firstName(body.firstName)
                .lastName(body.lastName)
                .email(body.email)
                .role(Role.ROLE_USER)
                .build();

        user = authService.registerUser(user);

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Register a new administrator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin registered"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }) })
    @PostMapping("/registerAdmin")
    public ResponseEntity<?> registerAdmin(@RequestBody @Valid AuthController.RegisterUserDto body) {

        User user = User.builder()
                .username(body.username)
                .password(passwordEncoder.encode((body.password)))
                .firstName(body.firstName)
                .lastName(body.lastName)
                .email(body.email)
                .role(Role.ROLE_ADMIN)
                .build();

        user = authService.registerUser(user);

        return ResponseEntity.ok(user);
    }
}