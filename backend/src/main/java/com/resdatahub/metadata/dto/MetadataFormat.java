package com.resdatahub.metadata.dto;

import org.apache.jena.riot.RDFFormat;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;

import java.util.List;
import java.util.Optional;

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

    public static MetadataFormat fromAcceptHeader(String acceptHeader) {
        if (acceptHeader == null || acceptHeader.isBlank()) {
            return TURTLE;
        }

        List<MediaType> mediaTypes = MediaType.parseMediaTypes(acceptHeader);
        MimeTypeUtils.sortBySpecificity(mediaTypes);

        return mediaTypes.stream()
                .map(MetadataFormat::fromMediaType)
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(TURTLE);
    }

    private static Optional<MetadataFormat> fromMediaType(MediaType mediaType) {
        if (mediaType.isWildcardType()) {
            return Optional.of(TURTLE);
        }

        for (MetadataFormat format : values()) {
            MediaType supportedMediaType = MediaType.parseMediaType(format.contentType);
            if (mediaType.isCompatibleWith(supportedMediaType)) {
                return Optional.of(format);
            }
        }

        return Optional.empty();
    }
}
