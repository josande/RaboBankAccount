package nl.crashandlearn.rabo_bankaccount.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import nl.crashandlearn.rabo_bankaccount.constraint.IbanFormat;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.model.Customer;
import nl.crashandlearn.rabo_bankaccount.service.AccountService;
import nl.crashandlearn.rabo_bankaccount.exception.AccountNotFoundException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("account")
public class AccountController {

    private final AccountService accountService;
    private final AccountAssembler accountAssembler;

    public AccountController(AccountService accountService, AccountAssembler accountAssembler) {
        this.accountService = accountService;
        this.accountAssembler = accountAssembler;
    }


    @Operation(summary = "Retrieves account details for all accounts",
            description = "Retrieve account details for all accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the account", content = { @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Account.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content) })

    @GetMapping("")
    CollectionModel<EntityModel<Account>> getAccounts() {
        var accounts = accountService.getAllAccounts().stream().map(accountAssembler::toModel).toList();

        Link link = linkTo(methodOn(AccountController.class)
                .getAccounts())
                .withSelfRel().expand();
        return CollectionModel.of(accounts, link);
    }

    @Operation(summary = "Retrieves account details",
            description = "Retrieve account details for the given account ID, or return 404 if no such account found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the account", content = { @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Account.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content) })
    @GetMapping("/{id}")
    EntityModel<Account> getAccountById(@PathVariable long id) {
        Account account = accountService.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        return accountAssembler.toModel(account);
    }

    public record NewAccount(

            @Schema(description = "Id of the customer owning this account.", example = "1234") @NotNull @Positive Long customerId,
            @Schema(example = IbanFormat.IBAN_EXAMPLE) @IbanFormat String iban,
            @Schema(description = "Current account balance in €", example = "123.01") @PositiveOrZero double balance) {}

    @Operation(summary = "Create a new account")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Account created")})
    @PostMapping("")
    ResponseEntity<?> createAccount(@io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Account to create", required = true,
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = NewAccount.class)
                ))
            @RequestBody @Valid NewAccount account) {

        EntityModel<Account> entityModel = accountAssembler.toModel(
                accountService.createAccount(Account.builder()
                        .customer(Customer.builder().id(account.customerId).build())
                        .iban(account.iban)
                        .balance(account.balance)
                        .build()));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @Operation(summary = "Delete an account by its id")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Account removed if found")})
    @DeleteMapping("/{id}")
    void deleteAccount(@PathVariable Long id) {
        accountService.delete(id);
    }


    @Operation(summary = "Updates an account",
            description = "Updates the account with given ID if found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated ",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Account.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = { @Content })
    })
    @PutMapping("/{id}")
    EntityModel<Account> updateAccount(@RequestBody Account account) {
        Account updatedAccount = accountService.update(account)
                .orElseThrow(() -> new AccountNotFoundException(account.getId()));;
        return accountAssembler.toModel(updatedAccount);
    }
}
