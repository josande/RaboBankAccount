package nl.crashandlearn.rabo_bankaccount.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import nl.crashandlearn.rabo_bankaccount.exception.ErrorDto;
import nl.crashandlearn.rabo_bankaccount.model.Account;
import nl.crashandlearn.rabo_bankaccount.service.AccountService;
import nl.crashandlearn.rabo_bankaccount.exception.AccountNotFoundException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "Retrieves all account details for the users accounts",
            description = "Returns a list of all account for current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok",
                content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Account.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found",
                content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }) })
    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_USER')")
    CollectionModel<EntityModel<Account>> getAccountsForUser() {
        var accounts = accountService.getAllAccountsForUser().stream().map(accountAssembler::toModel).toList();

        Link link = linkTo(methodOn(AccountController.class)
                .getAccountsForUser())
                .withSelfRel().expand();
        return CollectionModel.of(accounts, link);
    }


    @Operation(summary = "Retrieves account details",
            description = "Retrieve account details for the given account ID, or return 404 if no such account found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the account",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Account.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }) })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")

    EntityModel<Account> getAccountById(@PathVariable long id) {
        Account account = accountService.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        return accountAssembler.toModel(account);
    }

    @Operation(summary = "Retrieves account details for all users",
            description = "This functionality is only for Admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Account.class))})
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    CollectionModel<EntityModel<Account>> getAllAccounts() {
        var accounts = accountService.getAllAccounts().stream().map(accountAssembler::toModel).toList();

        Link link = linkTo(methodOn(AccountController.class)
                .getAllAccounts())
                .withSelfRel().expand();
        return CollectionModel.of(accounts, link);
    }

    public record NewAccountDto(
            @Schema(description = "Current account balance in €", example = "100.00") @PositiveOrZero double balance) {}

    @Operation(summary = "Create a new account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("")
    ResponseEntity<?> createAccount(@RequestBody @Valid NewAccountDto account) {

        EntityModel<Account> entityModel = accountAssembler.toModel(
                accountService.createAccount(account.balance));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }
    @Operation(summary = "Delete an account by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account removed if found", content = { @Content }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User is not allowed to delete this account",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }) })
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{id}")
    void deleteAccount(@PathVariable Long id) {
        accountService.delete(id);
    }

    record WithdrawDto(Long accountIdFrom, @Positive double amount) {}
    @Operation(summary = "Withdraw money from an account",
            description = "Account must hold sufficient funds.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer complete", content = { @Content }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User is not perform this transfer",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "403", description = "Transfer not allowed",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @PutMapping("/withdraw")
    void withdraw(@RequestBody WithdrawDto dto) {
        accountService.accountWithdrawal(dto.amount, dto.accountIdFrom);
    }

    record TransferDto(Long accountIdFrom, Long accountIdTo,  @Positive double amount) {}
    @Operation(summary = "Transfer money between two accounts",
            description = "Both accounts must be in this bank.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer complete", content = { @Content }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User is not perform this transfer",
                content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "403", description = "Transfer not allowed",
                content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Account not found",
                content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @PutMapping("/transfer")
    void transfer(@RequestBody TransferDto dto) {
        accountService.accountTransfer(dto.amount, dto.accountIdFrom, dto.accountIdTo);
    }
}
