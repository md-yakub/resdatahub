package com.resdatahub.knowledgegraph.service;

import com.resdatahub.knowledgegraph.dto.SparqlQueryResponse;
import com.resdatahub.knowledgegraph.validation.SparqlQueryException;
import com.resdatahub.knowledgegraph.validation.SparqlQueryValidator;
import com.resdatahub.metadata.rdf.RdfMetadataBuilder;
import com.resdatahub.metadata.service.CatalogMetadataService;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SparqlServiceTest {

    @Mock
    private CatalogMetadataService catalogMetadataService;

    private SparqlService sparqlService;

    @BeforeEach
    void setUp() {
        sparqlService = new SparqlService(catalogMetadataService, new SparqlQueryValidator());
    }

    @Test
    void validSelectQuerySucceeds() {
        when(catalogMetadataService.buildLatestPublishedCatalogModel()).thenReturn(modelWithDatasets(2));

        SparqlQueryResponse response = sparqlService.executeSelect("""
                PREFIX dcat: <http://www.w3.org/ns/dcat#>
                PREFIX dct: <http://purl.org/dc/terms/>

                SELECT ?dataset ?title
                WHERE {
                  ?dataset a dcat:Dataset ;
                           dct:title ?title .
                }
                ORDER BY ?title
                """);

        assertThat(response.variables()).containsExactly("dataset", "title");
        assertThat(response.rows()).hasSize(2);
        assertThat(response.rows().get(0).get("title")).isEqualTo("Dataset 1");
        assertThat(response.truncated()).isFalse();
    }

    @Test
    void invalidQueryIsRejected() {
        assertThatThrownBy(() -> sparqlService.executeSelect("SELECT WHERE {"))
                .isInstanceOf(SparqlQueryException.class)
                .hasMessageContaining("Invalid SPARQL query");
    }

    @Test
    void updateInsertAndDeleteQueriesAreRejected() {
        assertThatThrownBy(() -> sparqlService.executeSelect("INSERT DATA { <urn:s> <urn:p> <urn:o> }"))
                .isInstanceOf(SparqlQueryException.class)
                .hasMessage("SPARQL Update operations are not supported");

        assertThatThrownBy(() -> sparqlService.executeSelect("DELETE DATA { <urn:s> <urn:p> <urn:o> }"))
                .isInstanceOf(SparqlQueryException.class)
                .hasMessage("SPARQL Update operations are not supported");
    }

    @Test
    void nonSelectQueryTypesAreRejected() {
        assertThatThrownBy(() -> sparqlService.executeSelect("ASK { ?s ?p ?o }"))
                .isInstanceOf(SparqlQueryException.class)
                .hasMessage("Only SPARQL SELECT queries are supported");
    }

    @Test
    void serviceClausesAreRejected() {
        assertThatThrownBy(() -> sparqlService.executeSelect("""
                SELECT ?s
                WHERE {
                  SERVICE <https://example.org/sparql> {
                    ?s ?p ?o .
                  }
                }
                """))
                .isInstanceOf(SparqlQueryException.class)
                .hasMessage("SERVICE clauses are not supported");
    }

    @Test
    void rowLimitIsEnforcedWhenQueryHasNoLimit() {
        when(catalogMetadataService.buildLatestPublishedCatalogModel()).thenReturn(modelWithDatasets(501));

        SparqlQueryResponse response = sparqlService.executeSelect("""
                PREFIX dcat: <http://www.w3.org/ns/dcat#>
                PREFIX dct: <http://purl.org/dc/terms/>

                SELECT ?dataset ?title
                WHERE {
                  ?dataset a dcat:Dataset ;
                           dct:title ?title .
                }
                """);

        assertThat(response.rowCount()).isEqualTo(500);
        assertThat(response.rows()).hasSize(500);
        assertThat(response.truncated()).isTrue();
    }

    @Test
    void rowLimitIsEnforcedWhenQueryLimitIsTooHigh() {
        when(catalogMetadataService.buildLatestPublishedCatalogModel()).thenReturn(modelWithDatasets(501));

        SparqlQueryResponse response = sparqlService.executeSelect("""
                PREFIX dcat: <http://www.w3.org/ns/dcat#>
                PREFIX dct: <http://purl.org/dc/terms/>

                SELECT ?dataset ?title
                WHERE {
                  ?dataset a dcat:Dataset ;
                           dct:title ?title .
                }
                LIMIT 1000
                """);

        assertThat(response.rowCount()).isEqualTo(500);
        assertThat(response.truncated()).isTrue();
    }

    private Model modelWithDatasets(int count) {
        Model model = ModelFactory.createDefaultModel();

        for (int index = 1; index <= count; index++) {
            model.createResource("http://localhost:8080/id/dataset/%03d".formatted(index))
                    .addProperty(RDF.type, model.createResource(RdfMetadataBuilder.DCAT + "Dataset"))
                    .addProperty(
                            model.createProperty(RdfMetadataBuilder.DCT, "title"),
                            "Dataset %d".formatted(index)
                    );
        }

        return model;
    }
}
