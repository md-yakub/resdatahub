package com.resdatahub.knowledgegraph.controller;

import com.resdatahub.common.exception.ErrorResponse;
import com.resdatahub.knowledgegraph.dto.SparqlExampleResponse;
import com.resdatahub.knowledgegraph.dto.SparqlQueryRequest;
import com.resdatahub.knowledgegraph.dto.SparqlQueryResponse;
import com.resdatahub.knowledgegraph.service.SparqlService;
import com.resdatahub.knowledgegraph.validation.SparqlQueryException;
import com.resdatahub.knowledgegraph.validation.SparqlQueryTimeoutException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/public/sparql")
public class SparqlController {

    private final SparqlService sparqlService;

    public SparqlController(SparqlService sparqlService) {
        this.sparqlService = sparqlService;
    }

    @PostMapping
    @Operation(
            summary = "Run a read-only SPARQL SELECT query",
            description = "Runs a safe SELECT query against an in-memory RDF graph built from the latest published dataset versions.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SparqlQueryRequest.class),
                            examples = @ExampleObject(
                                    name = "Dataset titles",
                                    value = """
                                            {
                                              "query": "PREFIX dcat: <http://www.w3.org/ns/dcat#> PREFIX dct: <http://purl.org/dc/terms/> SELECT ?dataset ?title WHERE { ?dataset a dcat:Dataset ; dct:title ?title . } LIMIT 25"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SPARQL query executed.",
                            content = @Content(schema = @Schema(implementation = SparqlQueryResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid or unsupported SPARQL query.", content = @Content),
                    @ApiResponse(responseCode = "408", description = "SPARQL query timed out.", content = @Content)
            }
    )
    public SparqlQueryResponse executeSelect(
            @Valid @org.springframework.web.bind.annotation.RequestBody SparqlQueryRequest request
    ) {
        return sparqlService.executeSelect(request.query());
    }

    @GetMapping("/examples")
    @Operation(
            summary = "Get SPARQL query examples",
            description = "Returns example SELECT queries for the public ResDataHub knowledge graph.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "SPARQL examples returned.",
                    content = @Content(schema = @Schema(implementation = SparqlExampleResponse.class))
            )
    )
    public List<SparqlExampleResponse> getExamples() {
        return sparqlService.getExamples();
    }

    @ExceptionHandler(SparqlQueryException.class)
    public ResponseEntity<ErrorResponse> handleSparqlQueryException(
            SparqlQueryException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(SparqlQueryTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleSparqlQueryTimeoutException(
            SparqlQueryTimeoutException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.REQUEST_TIMEOUT, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "SPARQL query is required", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI()
                ));
    }
}
