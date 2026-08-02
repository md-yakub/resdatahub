package com.resdatahub.file.service;

import com.resdatahub.common.exception.ConflictException;
import com.resdatahub.common.exception.ResourceNotFoundException;
import com.resdatahub.dataset.entity.Dataset;
import com.resdatahub.dataset.repository.DatasetRepository;
import com.resdatahub.file.dto.DatasetFileDownload;
import com.resdatahub.file.dto.DatasetFileResponse;
import com.resdatahub.file.entity.DatasetFile;
import com.resdatahub.file.entity.DatasetFileCategory;
import com.resdatahub.file.mapper.DatasetFileMapper;
import com.resdatahub.file.repository.DatasetFileRepository;
import com.resdatahub.file.storage.DownloadedDatasetFile;
import com.resdatahub.file.storage.MinioStorageService;
import com.resdatahub.file.storage.StoredDatasetFile;
import com.resdatahub.version.entity.DatasetVersion;
import com.resdatahub.version.entity.DatasetVersionStatus;
import com.resdatahub.version.repository.DatasetVersionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class DatasetFileService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final DatasetFileMapper datasetFileMapper;
    private final MinioStorageService minioStorageService;
    private final long maxFileSizeBytes;

    public DatasetFileService(
            DatasetRepository datasetRepository,
            DatasetVersionRepository datasetVersionRepository,
            DatasetFileRepository datasetFileRepository,
            DatasetFileMapper datasetFileMapper,
            MinioStorageService minioStorageService,
            @Value("${resdatahub.storage.max-file-size-bytes}") long maxFileSizeBytes
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.datasetFileMapper = datasetFileMapper;
        this.minioStorageService = minioStorageService;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Transactional
    public DatasetFileResponse uploadFile(
            UUID datasetId,
            UUID versionId,
            MultipartFile file,
            DatasetFileCategory category
    ) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);
        validateUpload(file, category);

        StoredDatasetFile storedFile = minioStorageService.store(file, datasetId, versionId);

        DatasetFile datasetFile = new DatasetFile();
        datasetFile.setDatasetVersion(version);
        datasetFile.setOriginalFilename(cleanOriginalFilename(file.getOriginalFilename()));
        datasetFile.setStorageKey(storedFile.storageKey());
        datasetFile.setContentType(storedFile.contentType());
        datasetFile.setFileSize(storedFile.fileSize());
        datasetFile.setSha256(storedFile.sha256());
        datasetFile.setCategory(category);

        DatasetFile savedFile = datasetFileRepository.save(datasetFile);
        return datasetFileMapper.toResponse(savedFile);
    }

    @Transactional(readOnly = true)
    public List<DatasetFileResponse> getFiles(UUID datasetId, UUID versionId) {
        findVersion(datasetId, versionId);

        return datasetFileRepository.findByDatasetVersionIdOrderByCreatedAtDesc(versionId)
                .stream()
                .map(datasetFileMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatasetFileDownload downloadFile(UUID datasetId, UUID versionId, UUID fileId) {
        findVersion(datasetId, versionId);
        DatasetFile datasetFile = findFile(versionId, fileId);
        DownloadedDatasetFile downloadedFile = minioStorageService.download(datasetFile);

        return new DatasetFileDownload(
                new InputStreamResource(downloadedFile.inputStream()),
                datasetFile.getOriginalFilename(),
                downloadedFile.contentType(),
                downloadedFile.fileSize()
        );
    }

    @Transactional
    public void deleteFile(UUID datasetId, UUID versionId, UUID fileId) {
        DatasetVersion version = findVersion(datasetId, versionId);
        ensureDraft(version);

        DatasetFile datasetFile = findFile(versionId, fileId);
        minioStorageService.delete(datasetFile.getStorageKey());
        datasetFileRepository.delete(datasetFile);
    }

    private DatasetVersion findVersion(UUID datasetId, UUID versionId) {
        if (!datasetRepository.existsById(datasetId)) {
            throw new ResourceNotFoundException("Dataset not found");
        }

        return datasetVersionRepository.findByIdAndDatasetId(versionId, datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset version not found"));
    }

    private DatasetFile findFile(UUID versionId, UUID fileId) {
        return datasetFileRepository.findByIdAndDatasetVersionId(fileId, versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset file not found"));
    }

    private void ensureDraft(DatasetVersion version) {
        if (version.getStatus() != DatasetVersionStatus.DRAFT) {
            throw new ConflictException("Only DRAFT dataset versions can be changed");
        }
    }

    private void validateUpload(MultipartFile file, DatasetFileCategory category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (category == null) {
            throw new IllegalArgumentException("File category is required");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("File size must not exceed 50 MB");
        }
    }

    private String cleanOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "uploaded-file";
        }

        return originalFilename.replace("\\", "/")
                .substring(originalFilename.replace("\\", "/").lastIndexOf('/') + 1);
    }
}
