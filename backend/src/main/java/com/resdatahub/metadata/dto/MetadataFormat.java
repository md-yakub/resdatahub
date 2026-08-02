package com.resdatahub.metadata.dto;

import org.apache.jena.riot.RDFFormat;

public enum MetadataFormat {
    TURTLE("text/turtle", RDFFormat.TURTLE_PRETTY),
    JSON_LD("application/ld+json", RDFFormat.JSONLD_PRETTY),
    RDF_XML("application/rdf+xml", RDFFormat.RDFXML_PRETTY);

    private final String contentType;
    private final RDFFormat rdfFormat;

    MetadataFormat(String contentType, RDFFormat rdfFormat) {
        this.contentType = contentType;
        this.rdfFormat = rdfFormat;
    }

    public String getContentType() {
        return contentType;
    }

    public RDFFormat getRdfFormat() {
        return rdfFormat;
    }
}
