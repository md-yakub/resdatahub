package com.resdatahub.knowledgegraph.service;

import com.resdatahub.knowledgegraph.dto.SparqlExampleResponse;
import com.resdatahub.knowledgegraph.dto.SparqlQueryResponse;
import com.resdatahub.knowledgegraph.dto.SparqlResultRow;
import com.resdatahub.knowledgegraph.validation.SparqlQueryTimeoutException;
import com.resdatahub.knowledgegraph.validation.SparqlQueryValidator;
import com.resdatahub.metadata.rdf.RdfMetadataBuilder;
import com.resdatahub.metadata.service.CatalogMetadataService;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SparqlService {

    private static final int MAX_ROWS = 500;
    private static final long QUERY_TIMEOUT_SECONDS = 5;

    private final CatalogMetadataService catalogMetadataService;
    private final SparqlQueryValidator sparqlQueryValidator;

    public SparqlService(
            CatalogMetadataService catalogMetadataService,
            SparqlQueryValidator sparqlQueryValidator
    ) {
        this.catalogMetadataService = catalogMetadataService;
        this.sparqlQueryValidator = sparqlQueryValidator;
    }

    public SparqlQueryResponse executeSelect(String queryText) {
        Query query = sparqlQueryValidator.validateSelectQuery(queryText);
        boolean cappedByService = applyLimit(query);
        Model model = catalogMetadataService.buildLatestPublishedCatalogModel();

        try (QueryExecution queryExecution = QueryExecution.create()
                .query(query)
                .model(model)
                .timeout(QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()) {
            ResultSet resultSet = queryExecution.execSelect();
            List<String> variables = resultSet.getResultVars();
            List<SparqlResultRow> rows = new java.util.ArrayList<>();

            while (resultSet.hasNext() && rows.size() < MAX_ROWS) {
                rows.add(toRow(resultSet.nextSolution(), variables));
            }

            boolean truncated = rows.size() == MAX_ROWS && (cappedByService || resultSet.hasNext());

            return new SparqlQueryResponse(
                    variables,
                    rows,
                    rows.size(),
                    truncated
            );
        } catch (QueryCancelledException exception) {
            throw new SparqlQueryTimeoutException("SPARQL query timed out after 5 seconds");
        }
    }

    public List<SparqlExampleResponse> getExamples() {
        return List.of(
                new SparqlExampleResponse(
                        "All datasets and titles",
                        """
                        PREFIX dcat: <%s>
                        PREFIX dct: <%s>

                        SELECT ?dataset ?title
                        WHERE {
                          ?dataset a dcat:Dataset ;
                                   dct:title ?title .
                        }
                        LIMIT 25
                        """.formatted(RdfMetadataBuilder.DCAT, RdfMetadataBuilder.DCT)
                ),
                new SparqlExampleResponse(
                        "Datasets by keyword",
                        """
                        PREFIX dcat: <%s>
                        PREFIX dct: <%s>

                        SELECT ?dataset ?title ?keyword
                        WHERE {
                          ?dataset a dcat:Dataset ;
                                   dct:title ?title ;
                                   dcat:keyword ?keyword .
                          FILTER(LCASE(STR(?keyword)) = "arctic")
                        }
                        LIMIT 25
                        """.formatted(RdfMetadataBuilder.DCAT, RdfMetadataBuilder.DCT)
                ),
                new SparqlExampleResponse(
                        "Datasets and publishers",
                        """
                        PREFIX dcat: <%s>
                        PREFIX dct: <%s>
                        PREFIX foaf: <%s>

                        SELECT ?dataset ?title ?publisherName
                        WHERE {
                          ?dataset a dcat:Dataset ;
                                   dct:title ?title ;
                                   dct:publisher ?publisher .
                          ?publisher foaf:name ?publisherName .
                        }
                        LIMIT 25
                        """.formatted(RdfMetadataBuilder.DCAT, RdfMetadataBuilder.DCT, RdfMetadataBuilder.FOAF)
                ),
                new SparqlExampleResponse(
                        "Datasets and creators",
                        """
                        PREFIX dcat: <%s>
                        PREFIX dct: <%s>
                        PREFIX foaf: <%s>

                        SELECT ?dataset ?title ?givenName ?familyName
                        WHERE {
                          ?dataset a dcat:Dataset ;
                                   dct:title ?title ;
                                   dct:creator ?creator .
                          ?creator foaf:givenName ?givenName ;
                                   foaf:familyName ?familyName .
                        }
                        LIMIT 25
                        """.formatted(RdfMetadataBuilder.DCAT, RdfMetadataBuilder.DCT, RdfMetadataBuilder.FOAF)
                ),
                new SparqlExampleResponse(
                        "Datasets and distributions",
                        """
                        PREFIX dcat: <%s>
                        PREFIX dct: <%s>

                        SELECT ?dataset ?title ?distribution ?downloadUrl
                        WHERE {
                          ?dataset a dcat:Dataset ;
                                   dct:title ?title ;
                                   dcat:distribution ?distribution .
                          ?distribution dcat:downloadURL ?downloadUrl .
                        }
                        LIMIT 25
                        """.formatted(RdfMetadataBuilder.DCAT, RdfMetadataBuilder.DCT)
                )
        );
    }

    private boolean applyLimit(Query query) {
        if (!query.hasLimit()) {
            query.setLimit(MAX_ROWS);
            return true;
        }

        if (query.getLimit() > MAX_ROWS) {
            query.setLimit(MAX_ROWS);
            return true;
        }

        return false;
    }

    private SparqlResultRow toRow(QuerySolution solution, List<String> variables) {
        SparqlResultRow row = new SparqlResultRow();

        for (String variable : variables) {
            RDFNode value = solution.get(variable);
            row.put(variable, toValue(value));
        }

        return row;
    }

    private String toValue(RDFNode node) {
        if (node == null) {
            return null;
        }

        if (node.isURIResource()) {
            return node.asResource().getURI();
        }

        if (node.isLiteral()) {
            Literal literal = node.asLiteral();
            return literal.getLexicalForm();
        }

        if (node.isAnon()) {
            Resource resource = node.asResource();
            return "_:" + resource.getId().getLabelString();
        }

        return node.toString();
    }
}
