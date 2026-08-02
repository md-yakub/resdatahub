package com.resdatahub.file.storage;

import com.resdatahub.file.entity.DatasetFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class MinioStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "csv",
            "json",
            "xlsx",
            "zip",
            "pdf",
            "txt",
            "geojson"
    );

    private static final Set<String> EXECUTABLE_EXTENSIONS = Set.of(
            "exe",
            "dll",
            "bat",
            "cmd",
            "com",
            "msi",
            "sh",
            "jar",
            "ps1",
            "scr"
    );

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageService(
            @Value("${resdatahub.minio.endpoint}") String endpoint,
            @Value("${resdatahub.minio.access-key}") String accessKey,
            @Value("${resdatahub.minio.secret-key}") String secretKey,
            @Value("${resdatahub.minio.bucket}") String bucketName
    ) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucketName = bucketName;
    }

    public StoredDatasetFile store(MultipartFile file, UUID datasetId, UUID versionId) {
        validateFile(file);
        String storageKey = buildStorageKey(datasetId, versionId, file.getOriginalFilename());
        String contentType = getContentType(file);
        Path tempFile = null;

        try {
            tempFile = Files.createTempFile("resdatahub-upload-", ".tmp");
            String sha256 = copyAndHash(file, tempFile);
            ensureBucketExists();

            try (InputStream inputStream = Files.newInputStream(tempFile)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storageKey)
                                .stream(inputStream, file.getSize(), -1L)
                                .contentType(contentType)
                                .build()
                );
            }

            return new StoredDatasetFile(storageKey, contentType, file.getSize(), sha256);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not store dataset file", exception);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    public DownloadedDatasetFile download(DatasetFile datasetFile) {
        try {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(datasetFile.getStorageKey())
                            .build()
            );

            return new DownloadedDatasetFile(
                    response,
                    datasetFile.getContentType(),
                    datasetFile.getFileSize()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not download dataset file", exception);
        }
    }

    public void delete(String storageKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not delete dataset file", exception);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (EXECUTABLE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Executable files are not allowed");
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type is not allowed");
        }
    }

    private String buildStorageKey(UUID datasetId, UUID versionId, String originalFilename) {
        String extension = getExtension(originalFilename);
        return "datasets/%s/versions/%s/files/%s.%s".formatted(
                datasetId,
                versionId,
                UUID.randomUUID(),
                extension
        );
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String getContentType(MultipartFile file) {
        if (file.getContentType() == null || file.getContentType().isBlank()) {
            return "application/octet-stream";
        }

        return file.getContentType();
    }

    private String copyAndHash(MultipartFile file, Path tempFile)
            throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream inputStream = new DigestInputStream(file.getInputStream(), digest)) {
            Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        }
    }

    private void deleteTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException ignored) {
        }
    }
}
