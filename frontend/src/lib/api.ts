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
