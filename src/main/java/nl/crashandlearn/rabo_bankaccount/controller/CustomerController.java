package nl.crashandlearn.rabo_bankaccount.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import nl.crashandlearn.rabo_bankaccount.exception.CustomerNotFoundException;
import nl.crashandlearn.rabo_bankaccount.model.Customer;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.security.userservice.UserDetail;
import nl.crashandlearn.rabo_bankaccount.service.AccountService;
import nl.crashandlearn.rabo_bankaccount.service.CustomerService;
import org.springframework.context.annotation.Role;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("customer")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerAssembler customerAssembler;
    private final AccountService accountService;

    public CustomerController(CustomerService customerService, CustomerAssembler customerAssembler, AccountService accountService) {
        this.customerService = customerService;
        this.customerAssembler = customerAssembler;
        this.accountService = accountService;
    }

    @Operation(summary = "Retrieves customer details",
            description = "Retrieve customer details for the given customer ID, or return 404 if no such customer found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the customer", content = { @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Customer.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content) })
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
        return ResponseEntity.ok(customer);
    }

    public record NewCustomerBody(
            @Schema(example = "Jane") String firstName,
            @Schema(example = "Doe") String lastName,
            @Schema(example = "jane.doe@email.com") @Email String email) {}

    @Operation(summary = "Create a new customer")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Customer created")})
    @PostMapping("")
    ResponseEntity<?> createCustomer(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Customer to create", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = NewCustomerBody.class)))
                                     @RequestBody @Valid CustomerController.NewCustomerBody customer,
                                     Authentication authentication) {
        EntityModel<Customer> entityModel = customerAssembler.toModel(customerService.createCustomer(
                Customer.builder()
                        .firstName(customer.firstName)
                        .lastName(customer.lastName)
                        .email(customer.email)
                        .user(User.builder().id(((UserDetail) authentication.getPrincipal()).getId()).build())
                        .build()));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    record AddAccountBody(long customerId) {}
    @Operation(summary = "Add a new account to user")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Account added")})
    @PutMapping("/addAccount")
    EntityModel<Customer> addAccount(@RequestBody AddAccountBody body) {
        Customer updatedCustomer = customerService.addAccount(body.customerId)
                .orElseThrow(() -> new CustomerNotFoundException(body.customerId));
        return customerAssembler.toModel(updatedCustomer);
    }

    record RemoveAccountBody(long customerId, long accountId) {}
    @Operation(summary = "Removes one of the users account")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Account removed")})
    @DeleteMapping("/removeAccount")
    EntityModel<Customer> removeAccount(@RequestBody RemoveAccountBody body) {
        accountService.delete(body.accountId);
        Customer updatedCustomer = customerService.addAccount(body.customerId)
                .orElseThrow(() -> new CustomerNotFoundException(body.customerId));
        return customerAssembler.toModel(updatedCustomer);

    }

}
