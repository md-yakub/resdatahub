# ResDataHub DCAT/DCAT-AP Gap Analysis

This document compares the current ResDataHub metadata model with a practical DCAT/DCAT-AP harvesting profile. It is a portfolio implementation guide and not an official DCAT-AP certification or compliance audit.

## Catalog

| Property | Status | Current ResDataHub source | Possible future improvement |
| --- | --- | --- | --- |
| title | Supported | `resdatahub.catalog.title` | Add multilingual catalog titles. |
| description | Supported | `resdatahub.catalog.description` | Add richer scope and curation policy text. |
| publisher | Supported | `resdatahub.catalog.publisher-name`, optional `resdatahub.catalog.publisher-uri` | Add a managed publisher organization record. |
| homepage | Supported | `resdatahub.catalog.homepage` | Add a public frontend catalog landing page. |
| language | Supported | `resdatahub.catalog.language` | Allow per-dataset and multilingual metadata languages. |
| issued | Supported | `resdatahub.catalog.issued` | Track real catalog launch date in deployment configuration. |
| modified | Supported | Latest `updatedAt` among published dataset versions | Include other catalog-level changes when future catalog records exist. |
| datasets | Supported | Published `DatasetVersion` records | Add catalog filters, paging links, and dataset series support. |

## Dataset

| Property | Status | Current ResDataHub source | Possible future improvement |
| --- | --- | --- | --- |
| identifier | Supported | `Dataset.id` and `DatasetVersion.id` | Add DOI or Handle identifiers. |
| title | Supported | `DatasetVersion.title` | Add multilingual titles. |
| description | Supported | `DatasetVersion.description` | Add abstracts, methods, and provenance fields. |
| publisher | Supported | `Dataset.organization` | Add organization identifiers such as ROR. |
| creators | Supported | `DatasetCreator` ordered by `position` | Add contributor roles and creator identifiers beyond ORCID. |
| keywords | Supported | `DatasetKeyword.value` | Add controlled vocabularies and keyword language tags. |
| themes | Missing | No theme field exists | Add DCAT themes linked to a controlled theme taxonomy. |
| language | Missing | No dataset language field exists | Add a dataset metadata language field. |
| license | Supported | `DatasetVersion.license` | Support custom license text for `OTHER`. |
| issued | Supported | `DatasetVersion.publishedAt` | Add separate accepted and available dates if needed. |
| modified | Supported | `DatasetVersion.updatedAt` | Track metadata update timestamps separately from system updates. |
| contact point | Missing | No contact point field exists | Add contact name, email, and organization role fields. |
| spatial coverage | Missing | No spatial metadata exists | Add bounding boxes, place names, and geospatial identifiers. |
| temporal coverage | Missing | No temporal metadata exists | Add start and end dates for dataset coverage. |
| distributions | Supported | Uploaded `DatasetFile` records | Add richer distribution metadata and access policies. |

## Distribution

| Property | Status | Current ResDataHub source | Possible future improvement |
| --- | --- | --- | --- |
| title | Supported | `DatasetFile.originalFilename` | Add separate display titles for files. |
| access URL | Partially supported | Existing download endpoint can act as access/download URL | Add stable public landing pages per file or distribution. |
| download URL | Supported | Existing file download endpoint | Add signed/public access policy controls later if authentication is introduced. |
| license | Partially supported | Dataset version license applies to all files | Allow distribution-specific licenses where needed. |
| media type | Supported | `DatasetFile.contentType` | Improve server-side MIME detection. |
| format | Partially supported | File extension/category and media type | Add explicit controlled format field. |
| byte size | Supported | `DatasetFile.fileSize` | Add original and compressed size when processing pipelines exist. |
| checksum | Supported | `DatasetFile.sha256` | Represent checksum algorithm explicitly. |
| availability | Missing | No availability field exists | Add DCAT-AP availability values such as available, experimental, or discontinued. |

## Summary

The current implementation supports the core harvesting path for a published catalog: catalog metadata, published datasets, publishers, creators, keywords, licenses, distributions, download URLs, media types, byte sizes, and SHA-256 checksums.

The largest DCAT-AP gaps are controlled themes, dataset language, contact point, spatial coverage, temporal coverage, distribution format, and availability. These should be added as first-class domain fields before claiming deeper DCAT-AP alignment.
