package nl.crashandlearn.rabo_bankaccount.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import nl.crashandlearn.rabo_bankaccount.exception.ErrorDto;
import nl.crashandlearn.rabo_bankaccount.model.CardType;
import nl.crashandlearn.rabo_bankaccount.model.User;
import nl.crashandlearn.rabo_bankaccount.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@RestController
@RequestMapping("card")
public class CardController implements Serializable  {


    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    record NewCardDto(Long accountId, CardType cardType) {}
    @Operation(summary = "Creates a new card for an account",
            description = "Only one card can be linked to an account at one time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created",
                    content = { @Content( mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User lacks permission to see audit posts",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found for id",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @PutMapping("")
    void createCard(@RequestBody @Valid NewCardDto dto) {
        cardService.createCard(dto.accountId, dto.cardType);
    }

    @Operation(summary = "Remove a  card from an account",
            description = "Removes the card an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card removed",
                    content = { @Content( mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "401", description = "User lacks permission to see audit posts",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) }),
            @ApiResponse(responseCode = "404", description = "User not found for id",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)) })
    })
    @DeleteMapping("/{id}")
    void removeCard(@PathVariable @Positive Long cardId) {
        cardService.removeCard(cardId);
    }
}
