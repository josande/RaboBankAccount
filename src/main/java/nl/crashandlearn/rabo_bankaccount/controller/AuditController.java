package nl.crashandlearn.rabo_bankaccount.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import nl.crashandlearn.rabo_bankaccount.exception.ErrorDto;
import nl.crashandlearn.rabo_bankaccount.model.AuditPost;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.service.AuditService;
import nl.crashandlearn.rabo_bankaccount.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("audit")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AuditController {
    private final AuditService auditService;


    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "Retrieves all audit posts",
            description = "This method require the Admin role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All audit posts",
                    content = { @Content( mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User lacks permission to see audit posts",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @GetMapping("")
    public CollectionModel<AuditPost> getAllPosts() {
        List<AuditPost> posts = auditService.getAllAuditPosts();
        return CollectionModel.of(posts);
    }

    @Operation(summary = "Retrieves all audit posts regarding a user",
            description = "This method require the Admin role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All audit posts",
                    content = { @Content( mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User lacks permission to see audit posts",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found for id",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @GetMapping("/{id}")
    public CollectionModel<AuditPost> getAllPostsForUser(@PathVariable Long id) {
        List<AuditPost> posts = auditService.getAllAuditPostsForUser(id);
        return CollectionModel.of(posts);
    }

}
