import type {
  CatalogInfo,
  CatalogValidation,
  CitationFormat,
  CitationResponse,
  CreateDatasetCreatorRequest,
  CreateDatasetKeywordRequest,
  CreateDatasetRequest,
  DatasetCreatorResponse,
  DatasetFileCategory,
  DatasetFileResponse,
  DatasetKeywordResponse,
  DatasetResponse,
  DatasetVersionResponse,
  KnowledgeGraphData,
  KnowledgeGraphEdge,
  KnowledgeGraphNode,
  KnowledgeGraphNodeType,
  LicenseResponse,
  MetadataFormat,
  OrganizationResponse,
  PublicDatasetResponse,
  PublicSearchResponse,
  SearchSort,
  SparqlExampleResponse,
  SparqlQueryResponse,
  UpdateDatasetVersionRequest
} from "./types";

export interface SearchParams {
  q?: string;
  page?: number;
  size?: number;
  organizationId?: string;
  keyword?: string;
  licenseCode?: string;
  sort?: SearchSort;
}

export function getApiBaseUrl() {
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

  if (!baseUrl) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL is not configured");
  }

  return baseUrl.replace(/\/+$/, "");
}

export function buildBackendUrl(path: string, params?: Record<string, string | number | undefined>) {
  const url = new URL(path, getApiBaseUrl());

  Object.entries(params ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      url.searchParams.set(key, String(value));
    }
  });

  return url.toString();
}

export function buildMetadataUrl(datasetId: string, versionId: string, format: MetadataFormat) {
  return buildBackendUrl(`/api/public/datasets/${datasetId}/versions/${versionId}/metadata`, { format });
}

export function buildDownloadUrl(downloadUrl: string) {
  if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
    return downloadUrl;
  }

  return `${getApiBaseUrl()}${downloadUrl.startsWith("/") ? "" : "/"}${downloadUrl}`;
}

export async function getCatalogInfo() {
  return fetchJson<CatalogInfo>(buildBackendUrl("/api/public/catalog/info"));
}

export async function getCatalogValidation() {
  return fetchJson<CatalogValidation>(buildBackendUrl("/api/public/catalog/validation"));
}

export async function searchDatasets(params: SearchParams = {}) {
  return fetchJson<PublicSearchResponse>(
    buildBackendUrl("/api/public/search", {
      q: params.q,
      page: params.page ?? 0,
      size: params.size ?? 20,
      organizationId: params.organizationId,
      keyword: params.keyword,
      licenseCode: params.licenseCode,
      sort: params.sort ?? "NEWEST"
    })
  );
}

export async function getPublicDataset(datasetId: string, versionId: string) {
  return fetchJson<PublicDatasetResponse>(
    buildBackendUrl(`/api/public/datasets/${datasetId}/versions/${versionId}`)
  );
}

export async function getCitation(datasetId: string, versionId: string, format: CitationFormat) {
  return fetchJson<CitationResponse>(
    buildBackendUrl(`/api/public/datasets/${datasetId}/versions/${versionId}/citation`, { format })
  );
}

export async function getOrganizations() {
  return fetchJson<OrganizationResponse[]>(buildBackendUrl("/api/organizations"));
}

export async function getDatasets() {
  return fetchJson<DatasetResponse[]>(buildBackendUrl("/api/datasets"));
}

export async function getDataset(datasetId: string) {
  return fetchJson<DatasetResponse>(buildBackendUrl(`/api/datasets/${datasetId}`));
}

export async function createDataset(request: CreateDatasetRequest) {
  return requestJson<DatasetResponse>(`/api/datasets`, "POST", request);
}

export async function getDatasetVersion(datasetId: string, versionId: string) {
  return fetchJson<DatasetVersionResponse>(
    buildBackendUrl(`/api/datasets/${datasetId}/versions/${versionId}`)
  );
}

export async function updateDatasetVersion(datasetId: string, versionId: string, request: UpdateDatasetVersionRequest) {
  return requestJson<DatasetVersionResponse>(
    `/api/datasets/${datasetId}/versions/${versionId}`,
    "PATCH",
    request
  );
}

export async function getCreators(datasetId: string, versionId: string) {
  return fetchJson<DatasetCreatorResponse[]>(
    buildBackendUrl(`/api/datasets/${datasetId}/versions/${versionId}/creators`)
  );
}

export async function addCreator(datasetId: string, versionId: string, request: CreateDatasetCreatorRequest) {
  return requestJson<DatasetCreatorResponse>(
    `/api/datasets/${datasetId}/versions/${versionId}/creators`,
    "POST",
    request
  );
}

export async function updateCreator(
  datasetId: string,
  versionId: string,
  creatorId: string,
  request: CreateDatasetCreatorRequest
) {
  return requestJson<DatasetCreatorResponse>(
    `/api/datasets/${datasetId}/versions/${versionId}/creators/${creatorId}`,
    "PATCH",
    request
  );
}

export async function deleteCreator(datasetId: string, versionId: string, creatorId: string) {
  return requestNoContent(`/api/datasets/${datasetId}/versions/${versionId}/creators/${creatorId}`, "DELETE");
}

export async function getKeywords(datasetId: string, versionId: string) {
  return fetchJson<DatasetKeywordResponse[]>(
    buildBackendUrl(`/api/datasets/${datasetId}/versions/${versionId}/keywords`)
  );
}

export async function addKeyword(datasetId: string, versionId: string, request: CreateDatasetKeywordRequest) {
  return requestJson<DatasetKeywordResponse>(
    `/api/datasets/${datasetId}/versions/${versionId}/keywords`,
    "POST",
    request
  );
}

export async function deleteKeyword(datasetId: string, versionId: string, keywordId: string) {
  return requestNoContent(`/api/datasets/${datasetId}/versions/${versionId}/keywords/${keywordId}`, "DELETE");
}

export async function getLicenses() {
  return fetchJson<LicenseResponse[]>(buildBackendUrl("/api/licenses"));
}

export async function updateVersionLicense(datasetId: string, versionId: string, licenseId: string) {
  return requestJson<DatasetVersionResponse>(
    `/api/datasets/${datasetId}/versions/${versionId}/license`,
    "PATCH",
    { licenseId }
  );
}

export async function getFiles(datasetId: string, versionId: string) {
  return fetchJson<DatasetFileResponse[]>(
    buildBackendUrl(`/api/datasets/${datasetId}/versions/${versionId}/files`)
  );
}

export async function uploadFile(datasetId: string, versionId: string, file: File, category: DatasetFileCategory) {
  const formData = new FormData();
  formData.set("file", file);
  formData.set("category", category);

  return requestMultipart<DatasetFileResponse>(
    `/api/datasets/${datasetId}/versions/${versionId}/files`,
    formData
  );
}

export async function deleteFile(datasetId: string, versionId: string, fileId: string) {
  return requestNoContent(`/api/datasets/${datasetId}/versions/${versionId}/files/${fileId}`, "DELETE");
}

export async function publishVersion(datasetId: string, versionId: string) {
  return requestJson<DatasetVersionResponse>(
    `/api/datasets/${datasetId}/versions/${versionId}/publish`,
    "POST"
  );
}

export async function getSparqlExamples() {
  return fetchJson<SparqlExampleResponse[]>(buildBackendUrl("/api/public/sparql/examples"));
}

export async function executeSparqlQuery(query: string) {
  return requestJson<SparqlQueryResponse>("/api/public/sparql", "POST", { query });
}

export async function getKnowledgeGraphData(maxNodes = 100): Promise<KnowledgeGraphData> {
  const builder = new KnowledgeGraphBuilder(maxNodes);
  const responses = await Promise.all(KNOWLEDGE_GRAPH_QUERIES.map((query) => executeSparqlQuery(query)));

  responses[0].rows.forEach((row) => {
    builder.addDataset(row.dataset, row.title, row.versionNumber);
    builder.addNode(row.publisher, row.publisherName ?? "Organization", "organization", {
      shortName: row.publisherShortName,
      homepage: row.publisherHomepage
    });
    builder.addEdge(row.dataset, row.publisher, "PUBLISHED_BY", "Published by");
  });

  responses[1].rows.forEach((row) => {
    builder.addDataset(row.dataset, row.title, row.versionNumber);
    builder.addNode(row.creator, creatorLabel(row.givenName, row.familyName), "creator", {
      affiliation: row.affiliation,
      orcid: row.orcid
    });
    builder.addEdge(row.dataset, row.creator, "CREATED_BY", "Created by");
  });

  responses[2].rows.forEach((row) => {
    builder.addDataset(row.dataset, row.title, row.versionNumber);
    const keyword = cleanValue(row.keyword);

    if (keyword) {
      const keywordId = `keyword:${keyword.toLocaleLowerCase()}`;
      builder.addNode(keywordId, keyword, "keyword");
      builder.addEdge(row.dataset, keywordId, "HAS_KEYWORD", "Has keyword");
    }
  });

  responses[3].rows.forEach((row) => {
    builder.addDataset(row.dataset, row.title, row.versionNumber);
    builder.addNode(row.license, licenseLabel(row.license), "license");
    builder.addEdge(row.dataset, row.license, "LICENSED_UNDER", "Licensed under");
  });

  responses[4].rows.forEach((row) => {
    builder.addDataset(row.dataset, row.title, row.versionNumber);
    builder.addNode(row.file, row.fileTitle ?? resourceLabel(row.file), "file", {
      downloadUrl: row.downloadUrl,
      contentType: row.contentType,
      fileSize: row.fileSize,
      sha256: row.sha256,
      category: row.category
    });
    builder.addEdge(row.dataset, row.file, "HAS_FILE", "Has file");
  });

  return builder.toData(responses.some((response) => response.truncated));
}

const KNOWLEDGE_GRAPH_QUERY_LIMIT = 100;

const KNOWLEDGE_GRAPH_QUERIES = [
  `
PREFIX dcat: <http://www.w3.org/ns/dcat#>
PREFIX dct: <http://purl.org/dc/terms/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
SELECT ?dataset ?title ?versionNumber ?publisher ?publisherName ?publisherShortName ?publisherHomepage
WHERE {
  ?dataset a dcat:Dataset ;
    dct:title ?title ;
    dcat:version ?versionNumber ;
    dct:publisher ?publisher .
  OPTIONAL { ?publisher foaf:name ?publisherName . }
  OPTIONAL { ?publisher foaf:nick ?publisherShortName . }
  OPTIONAL { ?publisher foaf:homepage ?publisherHomepage . }
}
LIMIT ${KNOWLEDGE_GRAPH_QUERY_LIMIT}
`,
  `
PREFIX dcat: <http://www.w3.org/ns/dcat#>
PREFIX dct: <http://purl.org/dc/terms/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX resdatahub: <https://resdatahub.example/vocab#>
SELECT ?dataset ?title ?versionNumber ?creator ?givenName ?familyName ?affiliation ?orcid
WHERE {
  ?dataset a dcat:Dataset ;
    dct:title ?title ;
    dcat:version ?versionNumber ;
    dct:creator ?creator .
  OPTIONAL { ?creator foaf:givenName ?givenName . }
  OPTIONAL { ?creator foaf:familyName ?familyName . }
  OPTIONAL { ?creator resdatahub:affiliation ?affiliation . }
  OPTIONAL { ?creator resdatahub:orcid ?orcid . }
}
LIMIT ${KNOWLEDGE_GRAPH_QUERY_LIMIT}
`,
  `
PREFIX dcat: <http://www.w3.org/ns/dcat#>
PREFIX dct: <http://purl.org/dc/terms/>
SELECT ?dataset ?title ?versionNumber ?keyword
WHERE {
  ?dataset a dcat:Dataset ;
    dct:title ?title ;
    dcat:version ?versionNumber ;
    dcat:keyword ?keyword .
}
LIMIT ${KNOWLEDGE_GRAPH_QUERY_LIMIT}
`,
  `
PREFIX dcat: <http://www.w3.org/ns/dcat#>
PREFIX dct: <http://purl.org/dc/terms/>
SELECT ?dataset ?title ?versionNumber ?license
WHERE {
  ?dataset a dcat:Dataset ;
    dct:title ?title ;
    dcat:version ?versionNumber ;
    dct:license ?license .
}
LIMIT ${KNOWLEDGE_GRAPH_QUERY_LIMIT}
`,
  `
PREFIX dcat: <http://www.w3.org/ns/dcat#>
PREFIX dct: <http://purl.org/dc/terms/>
PREFIX spdx: <http://spdx.org/rdf/terms#>
PREFIX resdatahub: <https://resdatahub.example/vocab#>
SELECT ?dataset ?title ?versionNumber ?file ?fileTitle ?downloadUrl ?contentType ?fileSize ?sha256 ?category
WHERE {
  ?dataset a dcat:Dataset ;
    dct:title ?title ;
    dcat:version ?versionNumber ;
    dcat:distribution ?file .
  OPTIONAL { ?file dct:title ?fileTitle . }
  OPTIONAL { ?file dcat:downloadURL ?downloadUrl . }
  OPTIONAL { ?file dcat:mediaType ?contentType . }
  OPTIONAL { ?file dcat:byteSize ?fileSize . }
  OPTIONAL { ?file spdx:checksumValue ?sha256 . }
  OPTIONAL { ?file resdatahub:fileCategory ?category . }
}
LIMIT ${KNOWLEDGE_GRAPH_QUERY_LIMIT}
`
];

class KnowledgeGraphBuilder {
  private readonly nodes = new Map<string, KnowledgeGraphNode>();
  private readonly edges = new Map<string, KnowledgeGraphEdge>();
  private truncated = false;

  constructor(private readonly maxNodes: number) {}

  addDataset(id: string | null, label: string | null, versionNumber: string | null) {
    this.addNode(id, label ?? "Published dataset", "dataset", {
      version: versionNumber
    });
  }

  addNode(
    id: string | null,
    label: string,
    type: KnowledgeGraphNodeType,
    details: Record<string, string | string[] | null | undefined> = {}
  ) {
    if (!id) {
      return;
    }

    const existingNode = this.nodes.get(id);

    if (existingNode) {
      this.nodes.set(id, {
        ...existingNode,
        details: {
          ...existingNode.details,
          ...details
        }
      });
      return;
    }

    if (this.nodes.size >= this.maxNodes) {
      this.truncated = true;
      return;
    }

    this.nodes.set(id, {
      id,
      label,
      graphLabel: graphLabel(label, type),
      type,
      uri: isHttpUrl(id) ? id : undefined,
      publicUrl: type === "dataset" ? datasetPublicUrl(id) : undefined,
      details
    });
  }

  addEdge(source: string | null, target: string | null, type: string, label: string) {
    if (!source || !target || !this.nodes.has(source) || !this.nodes.has(target)) {
      return;
    }

    const id = `${source}:${type}:${target}`;

    if (!this.edges.has(id)) {
      this.edges.set(id, { id, source, target, label, type });
    }
  }

  toData(sparqlTruncated: boolean): KnowledgeGraphData {
    return {
      nodes: Array.from(this.nodes.values()),
      edges: Array.from(this.edges.values()),
      truncated: this.truncated || sparqlTruncated,
      maxNodes: this.maxNodes
    };
  }
}

function cleanValue(value: string | null) {
  return value?.trim() || null;
}

function creatorLabel(givenName: string | null, familyName: string | null) {
  return [givenName, familyName].map(cleanValue).filter(Boolean).join(" ") || "Creator";
}

function resourceLabel(value: string | null) {
  if (!value) {
    return "Resource";
  }

  const cleaned = value.replace(/[\/#]+$/, "");
  return decodeURIComponent(cleaned.substring(Math.max(cleaned.lastIndexOf("/"), cleaned.lastIndexOf("#")) + 1));
}

function licenseLabel(value: string | null) {
  if (!value) {
    return "License";
  }

  const normalized = value.toLocaleLowerCase();

  if (normalized.includes("creativecommons.org/licenses/by-sa/4.0")) {
    return "CC-BY-SA-4.0";
  }

  if (normalized.includes("creativecommons.org/licenses/by/4.0")) {
    return "CC-BY-4.0";
  }

  if (normalized.includes("creativecommons.org/publicdomain/zero/1.0")) {
    return "CC0-1.0";
  }

  return resourceLabel(value);
}

function graphLabel(value: string, type: KnowledgeGraphNodeType) {
  const maxLength = type === "dataset" ? 42 : type === "file" ? 30 : 26;
  const cleaned = value.trim();

  if (cleaned.length <= maxLength) {
    return cleaned;
  }

  return `${cleaned.substring(0, maxLength - 1)}...`;
}

function datasetPublicUrl(uri: string | null) {
  const match = uri?.match(/\/id\/dataset\/([^/]+)\/version\/([^/]+)$/);

  if (!match) {
    return undefined;
  }

  return `/datasets/${match[1]}/versions/${match[2]}`;
}

function isHttpUrl(value: string) {
  return value.startsWith("http://") || value.startsWith("https://");
}

async function requestJson<T>(path: string, method: string, body?: unknown): Promise<T> {
  return fetchJson<T>(
    buildBackendUrl(path),
    {
      method,
      headers: body === undefined ? undefined : { "Content-Type": "application/json" },
      body: body === undefined ? undefined : JSON.stringify(body)
    }
  );
}

async function requestMultipart<T>(path: string, body: FormData): Promise<T> {
  return fetchJson<T>(
    buildBackendUrl(path),
    {
      method: "POST",
      body
    }
  );
}

async function requestNoContent(path: string, method: string): Promise<void> {
  const response = await fetch(buildBackendUrl(path), { method, cache: "no-store" });

  if (!response.ok) {
    throw await buildApiError(response);
  }
}

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, { cache: "no-store", ...init });

  if (!response.ok) {
    throw await buildApiError(response);
  }

  return response.json() as Promise<T>;
}

async function buildApiError(response: Response) {
  const text = await response.text();
  let message = response.statusText;

  if (text) {
    try {
      const body = JSON.parse(text) as { message?: string; error?: string };
      message = body.message ?? body.error ?? text;
    } catch {
      message = text;
    }
  }

  if (process.env.NODE_ENV === "development") {
    console.error("API request failed", {
      status: response.status,
      statusText: response.statusText,
      message
    });
  }

  return new Error(`Request failed with status ${response.status}: ${message}`);
}
