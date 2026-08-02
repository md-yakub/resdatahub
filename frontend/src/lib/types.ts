export type SearchSort = "NEWEST" | "OLDEST" | "TITLE_ASC" | "TITLE_DESC";
export type CitationFormat = "APA" | "TEXT" | "BIBTEX" | "RIS";
export type MetadataFormat = "TURTLE" | "JSON_LD" | "RDF_XML";
export type DatasetVersionStatus = "DRAFT" | "SUBMITTED" | "APPROVED" | "PUBLISHED" | "WITHDRAWN";
export type DatasetFileCategory = "RAW" | "PROCESSED" | "DOCUMENTATION" | "SUPPLEMENTARY";

export interface CatalogInfo {
  title: string;
  profile: string;
  metadataEndpoint: string;
  supportedFormats: string[];
  publicBaseUrl: string;
  contact: string | null;
}

export interface CatalogValidation {
  profile: string;
  conforms: boolean;
  checkedDatasets: number;
  violations: CatalogValidationViolation[];
}

export interface CatalogValidationViolation {
  resource: string;
  property: string;
  severity: "ERROR" | "WARNING";
  message: string;
}

export interface PublicSearchResponse {
  items: PublicSearchItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PublicSearchItem {
  datasetId: string;
  versionId: string;
  versionNumber: string;
  title: string;
  description: string;
  organization: SearchOrganizationSummary;
  creators: SearchCreatorSummary[];
  keywords: string[];
  licenseCode: string | null;
  publishedAt: string | null;
  landingPageUrl: string;
}

export interface SearchOrganizationSummary {
  id: string;
  name: string;
  shortName: string | null;
}

export interface SearchCreatorSummary {
  givenName: string;
  familyName: string;
  position: number;
}

export interface PublicDatasetResponse {
  datasetId: string;
  organization: PublicOrganization;
  version: PublicDatasetVersion;
  creators: PublicCreator[];
  keywords: PublicKeyword[];
  license: PublicLicense | null;
  files: PublicFile[];
}

export interface PublicOrganization {
  id: string;
  name: string;
  shortName: string | null;
  website: string | null;
}

export interface PublicDatasetVersion {
  versionId: string;
  versionNumber: string;
  title: string;
  description: string;
  changeNote: string;
  status: string;
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PublicCreator {
  id: string;
  givenName: string;
  familyName: string;
  affiliation: string | null;
  orcid: string | null;
  position: number;
}

export interface PublicKeyword {
  id: string;
  value: string;
}

export interface PublicLicense {
  id: string;
  code: string;
  name: string;
  uri: string;
}

export interface PublicFile {
  id: string;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  sha256: string;
  category: string;
  createdAt: string;
  downloadUrl: string;
}

export interface CitationResponse {
  format: CitationFormat;
  citation: string;
}

export interface OrganizationResponse {
  id: string;
  name: string;
  shortName: string | null;
  description: string | null;
  website: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface DatasetResponse {
  id: string;
  organization: DatasetOrganizationSummary;
  latestVersion: DatasetVersionSummary | null;
  createdAt: string;
  updatedAt: string;
}

export interface DatasetOrganizationSummary {
  id: string;
  name: string;
  shortName: string | null;
}

export interface DatasetVersionSummary {
  id: string;
  versionNumber: string;
  title: string;
  status: DatasetVersionStatus;
  publishedAt: string | null;
}

export interface CreateDatasetRequest {
  organizationId: string;
  title: string;
  description: string;
}

export interface DatasetVersionResponse {
  id: string;
  datasetId: string;
  versionNumber: string;
  title: string;
  description: string;
  changeNote: string | null;
  status: DatasetVersionStatus;
  license: LicenseResponse | null;
  createdAt: string;
  updatedAt: string;
  publishedAt: string | null;
}

export interface UpdateDatasetVersionRequest {
  versionNumber?: string;
  title?: string;
  description?: string;
  changeNote?: string;
}

export interface DatasetCreatorResponse {
  id: string;
  givenName: string;
  familyName: string;
  affiliation: string | null;
  orcid: string | null;
  position: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDatasetCreatorRequest {
  givenName: string;
  familyName: string;
  affiliation?: string;
  orcid?: string;
  position: number;
}

export type UpdateDatasetCreatorRequest = CreateDatasetCreatorRequest;

export interface DatasetKeywordResponse {
  id: string;
  value: string;
  createdAt: string;
}

export interface CreateDatasetKeywordRequest {
  value: string;
}

export interface LicenseResponse {
  id: string;
  code: string;
  name: string;
  uri: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DatasetFileResponse {
  id: string;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  sha256: string;
  category: DatasetFileCategory;
  createdAt: string;
}

export interface SparqlQueryRequest {
  query: string;
}

export interface SparqlQueryResponse {
  variables: string[];
  rows: SparqlResultRow[];
  rowCount: number;
  truncated: boolean;
}

export type SparqlResultRow = Record<string, string | null>;

export interface SparqlExampleResponse {
  title: string;
  query: string;
}
