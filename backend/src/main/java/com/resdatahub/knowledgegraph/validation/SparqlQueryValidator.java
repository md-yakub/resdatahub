package com.resdatahub.knowledgegraph.validation;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.update.UpdateFactory;
import org.springframework.stereotype.Component;

@Component
public class SparqlQueryValidator {

    public static final int MAX_QUERY_LENGTH = 10_000;

    public Query validateSelectQuery(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            throw new SparqlQueryException("SPARQL query is required");
        }

        if (queryText.length() > MAX_QUERY_LENGTH) {
            throw new SparqlQueryException("SPARQL query must not exceed 10000 characters");
        }

        Query query = parseQuery(queryText);

        if (!query.isSelectType()) {
            throw new SparqlQueryException("Only SPARQL SELECT queries are supported");
        }

        rejectService(query);

        return query;
    }

    private Query parseQuery(String queryText) {
        try {
            return QueryFactory.create(queryText, Syntax.syntaxSPARQL_11);
        } catch (QueryParseException exception) {
            rejectUpdateIfParseable(queryText);
            throw new SparqlQueryException("Invalid SPARQL query: " + exception.getMessage());
        }
    }

    private void rejectUpdateIfParseable(String queryText) {
        try {
            UpdateFactory.create(queryText);
            throw new SparqlQueryException("SPARQL Update operations are not supported");
        } catch (SparqlQueryException exception) {
            throw exception;
        } catch (RuntimeException ignored) {
            // Not a parseable SPARQL Update request. The original query parse error is returned.
        }
    }

    private void rejectService(Query query) {
        if (query.getQueryPattern() == null) {
            return;
        }

        ServiceDetector detector = new ServiceDetector();
        ElementWalker.walk(query.getQueryPattern(), detector);

        if (detector.hasService()) {
            throw new SparqlQueryException("SERVICE clauses are not supported");
        }
    }

    private static class ServiceDetector extends ElementVisitorBase {
        private boolean service;

        @Override
        public void visit(ElementService elementService) {
            this.service = true;
        }

        boolean hasService() {
            return service;
        }
    }
}
