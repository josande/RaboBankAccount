package nl.crashandlearn.rabo_bankaccount.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import nl.crashandlearn.rabo_bankaccount.exception.ErrorDto;
import nl.crashandlearn.rabo_bankaccount.exception.UserNotFoundException;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user")
@SecurityRequirement(name = "BearerAuthentication")
public class UserController {
    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Retrieves details for current user",
            description = "Retrieve details for the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information",
                    content = { @Content( mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })

    })
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public ResponseEntity<User> getCurrentUserData() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Retrieves total balance for all the users accounts",
            description = "Retrieves the balance from all accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information",
                    content = { @Content( mediaType = "application/json", schema = @Schema(implementation = Double.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/balance")
    public ResponseEntity<Double> getCurrentUserBalance() {
        return ResponseEntity.ok(userService.getBalance());
    }



    @Operation(summary = "Retrieves details for a specific users",
            description = "This method require the Admin role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information",
                    content = { @Content( mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User lacks permission to see this user",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findByIdWithAccounts(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return ResponseEntity.ok(user);
    }
    @Operation(summary = "Retrieves details for all users",
            description = "This method require the Admin role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information",
                    content = { @Content( mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User lacks permission to see this user",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public CollectionModel<User> getAllUsers() {
        List<User> users = userService.getAllUsersWithAccounts();
        return CollectionModel.of(users);
    }
}
