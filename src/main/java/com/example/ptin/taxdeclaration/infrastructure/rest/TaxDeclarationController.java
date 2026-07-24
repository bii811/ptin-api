package com.example.ptin.taxdeclaration.infrastructure.rest;

import com.example.ptin.shared.security.model.AuthenticatedPrincipal;
import com.example.ptin.shared.web.ApiResponse;
import com.example.ptin.taxdeclaration.domain.model.TaxDeclarationId;
import com.example.ptin.taxdeclaration.domain.port.in.DeclareTaxUseCase;
import com.example.ptin.taxdeclaration.domain.port.in.GetTaxDeclarationUseCase;
import com.example.ptin.taxdeclaration.domain.port.in.ListMyTaxDeclarationsUseCase;
import com.example.ptin.taxdeclaration.infrastructure.rest.request.DeclareTaxRequest;
import com.example.ptin.taxdeclaration.infrastructure.rest.response.TaxDeclarationResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tax-declarations")
public class TaxDeclarationController {

    private final DeclareTaxUseCase declareTaxUseCase;
    private final GetTaxDeclarationUseCase getTaxDeclarationUseCase;
    private final ListMyTaxDeclarationsUseCase listMyTaxDeclarationsUseCase;

    public TaxDeclarationController(
            DeclareTaxUseCase declareTaxUseCase,
            GetTaxDeclarationUseCase getTaxDeclarationUseCase,
            ListMyTaxDeclarationsUseCase listMyTaxDeclarationsUseCase) {
        this.declareTaxUseCase = declareTaxUseCase;
        this.getTaxDeclarationUseCase = getTaxDeclarationUseCase;
        this.listMyTaxDeclarationsUseCase = listMyTaxDeclarationsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaxDeclarationResponse> declare(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody DeclareTaxRequest request) {
        TaxDeclarationId id = declareTaxUseCase.declare(request.toCommand(principal.userId()));
        var declaration = getTaxDeclarationUseCase.getById(id, principal.userId());
        return ApiResponse.success(TaxDeclarationResponse.from(declaration));
    }

    @GetMapping("/me")
    public ApiResponse<List<TaxDeclarationResponse>> listMine(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        List<TaxDeclarationResponse> responses = listMyTaxDeclarationsUseCase.listMine(principal.userId()).stream()
                .map(TaxDeclarationResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<TaxDeclarationResponse> getById(
            @AuthenticationPrincipal AuthenticatedPrincipal principal, @PathVariable UUID id) {
        var declaration = getTaxDeclarationUseCase.getById(new TaxDeclarationId(id), principal.userId());
        return ApiResponse.success(TaxDeclarationResponse.from(declaration));
    }
}
