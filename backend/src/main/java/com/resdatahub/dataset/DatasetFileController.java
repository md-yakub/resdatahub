package com.resdatahub.dataset;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/datasets/{datasetId}/versions/{versionId}/files")
public class DatasetFileController {

    private final DatasetFileService datasetFileService;

    public DatasetFileController(DatasetFileService datasetFileService) {
        this.datasetFileService = datasetFileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload dataset file",
            description = "Uploads a file to a DRAFT dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Dataset file uploaded.",
                            content = @Content(schema = @Schema(implementation = DatasetFileResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid upload.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is immutable.", content = @Content)
            }
    )
    public DatasetFileResponse uploadFile(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @RequestParam MultipartFile file,
            @RequestParam DatasetFileCategory category
    ) {
        return datasetFileService.uploadFile(datasetId, versionId, file, category);
    }

    @GetMapping
    @Operation(
            summary = "List dataset files",
            description = "Returns file metadata for a dataset version.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Dataset files returned.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DatasetFileResponse.class)))
                    ),
                    @ApiResponse(responseCode = "404", description = "Dataset or version not found.", content = @Content)
            }
    )
    public List<DatasetFileResponse> getFiles(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId
    ) {
        return datasetFileService.getFiles(datasetId, versionId);
    }

    @GetMapping("/{fileId}/download")
    @Operation(
            summary = "Download dataset file",
            description = "Downloads a stored dataset file.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Dataset file returned.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset, version, or file not found.", content = @Content)
            }
    )
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @PathVariable UUID fileId
    ) {
        DatasetFileDownload download = datasetFileService.downloadFile(datasetId, versionId, fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.fileSize())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.filename())
                                .build()
                                .toString()
                )
                .body(download.resource());
    }

    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete dataset file",
            description = "Deletes a file from MinIO and its metadata from PostgreSQL.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Dataset file deleted.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Dataset, version, or file not found.", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Version is immutable.", content = @Content)
            }
    )
    public void deleteFile(
            @PathVariable UUID datasetId,
            @PathVariable UUID versionId,
            @PathVariable UUID fileId
    ) {
        datasetFileService.deleteFile(datasetId, versionId, fileId);
    }
}
