package com.reparasuite.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reparasuite.api.exception.BadRequestException;
import com.reparasuite.api.exception.NotFoundException;

@Service
public class SecureUploadService {

  private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
  private static final Set<String> RECEIPT_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf", "webp");

  @Value("${reparasuite.upload-dir}")
  private String uploadDir;

  public record StoredFile(String url, String originalFilename, String storedFilename, String contentType) {}

  public StoredFile storeOtImage(UUID otId, MultipartFile file) throws IOException {
    return store(
        "ot",
        otId,
        file,
        IMAGE_EXTENSIONS,
        Set.of("image/jpeg", "image/png", "image/webp"),
        true
    );
  }

  public StoredFile storePaymentReceipt(UUID otId, MultipartFile file) throws IOException {
    return store(
        "pagos",
        otId,
        file,
        RECEIPT_EXTENSIONS,
        Set.of("image/jpeg", "image/png", "image/webp", "application/pdf"),
        false
    );
  }

  public StoredFile storeTicketImage(UUID ticketId, MultipartFile file) throws IOException {
    return store(
        "tickets",
        ticketId,
        file,
        IMAGE_EXTENSIONS,
        Set.of("image/jpeg", "image/png", "image/webp"),
        true
    );
  }

  public Resource loadOtImage(UUID otId, String filename) {
    return load("ot", otId, filename);
  }

  public Resource loadPaymentReceipt(UUID otId, String filename) {
    return load("pagos", otId, filename);
  }

  public Resource loadTicketImage(UUID ticketId, String filename) {
    return load("tickets", ticketId, filename);
  }

  public void deleteByUrl(String url) {
    if (url == null || url.isBlank()) return;

    try {
      String normalized = url.trim();
      String marker = "/api/v1/archivos/";
      int idx = normalized.indexOf(marker);
      if (idx < 0) return;

      String tail = normalized.substring(idx + marker.length()); // bucket/entityId/file
      String[] segments = tail.split("/");
      if (segments.length < 3) return;

      String bucket = segments[0];
      String entityId = segments[1];
      String filename = tail.substring((bucket + "/" + entityId + "/").length());

      if (!Set.of("ot", "pagos", "tickets").contains(bucket)) {
        return;
      }

      UUID id = UUID.fromString(entityId);
      Path file = buildPath(bucket, id, filename);
      Files.deleteIfExists(file);
    } catch (Exception ignored) {
    }
  }

  private StoredFile store(
      String bucket,
      UUID entityId,
      MultipartFile file,
      Set<String> allowedExtensions,
      Set<String> allowedMimeTypes,
      boolean onlyImages
  ) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("Archivo vacío");
    }

    String originalFilename = safeOriginalName(file.getOriginalFilename());
    String extension = getExtension(originalFilename);

    if (!allowedExtensions.contains(extension)) {
      throw new BadRequestException("Extensión de archivo no permitida");
    }

    byte[] bytes = file.getBytes();
    String detectedMime = detectMimeType(bytes);

    if (detectedMime == null) {
      throw new BadRequestException("No se pudo determinar el tipo real del archivo");
    }

    if (onlyImages && !detectedMime.startsWith("image/")) {
      throw new BadRequestException("Solo se permiten imágenes");
    }

    if (!allowedMimeTypes.contains(detectedMime)) {
      throw new BadRequestException("Tipo de archivo no permitido");
    }

    String storedFilename = UUID.randomUUID() + "." + extension;
    Path dir = buildDirectory(bucket, entityId);
    Files.createDirectories(dir);

    Path target = dir.resolve(storedFilename).normalize();
    if (!target.startsWith(dir)) {
      throw new BadRequestException("Ruta de archivo inválida");
    }

    Files.write(target, bytes, StandardOpenOption.CREATE_NEW);

    String url = "/api/v1/archivos/" + bucket + "/" + entityId + "/" + storedFilename;
    return new StoredFile(url, originalFilename, storedFilename, detectedMime);
  }

  private Resource load(String bucket, UUID entityId, String filename) {
    try {
      Path file = buildPath(bucket, entityId, filename);
      if (!Files.exists(file) || !Files.isRegularFile(file)) {
        throw new NotFoundException("Archivo no encontrado");
      }

      Resource resource = new UrlResource(file.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        throw new NotFoundException("Archivo no disponible");
      }

      return resource;
    } catch (IOException ex) {
      throw new NotFoundException("Archivo no encontrado");
    }
  }

  public String probeContentType(Resource resource, String fallbackFilename) {
    try {
      Path path = resource.getFile().toPath();
      String type = Files.probeContentType(path);
      if (type != null && !type.isBlank()) {
        return type;
      }
    } catch (Exception ignored) {
    }

    String ext = getExtension(fallbackFilename);
    return switch (ext) {
      case "jpg", "jpeg" -> "image/jpeg";
      case "png" -> "image/png";
      case "webp" -> "image/webp";
      case "pdf" -> "application/pdf";
      default -> "application/octet-stream";
    };
  }

  private Path buildDirectory(String bucket, UUID entityId) {
    try {
      return Paths.get(uploadDir).resolve(bucket).resolve(entityId.toString()).normalize();
    } catch (InvalidPathException ex) {
      throw new BadRequestException("Ruta de almacenamiento inválida");
    }
  }

  private Path buildPath(String bucket, UUID entityId, String filename) {
    String safeFilename = sanitizeStoredFilename(filename);
    Path dir = buildDirectory(bucket, entityId);
    Path file = dir.resolve(safeFilename).normalize();

    if (!file.startsWith(dir)) {
      throw new BadRequestException("Ruta de archivo inválida");
    }

    return file;
  }

  private String safeOriginalName(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return "archivo.bin";
    }

    String sanitized = originalFilename
        .replace("\\", "_")
        .replace("/", "_")
        .replaceAll("[^a-zA-Z0-9._-]", "_");

    return sanitized.isBlank() ? "archivo.bin" : sanitized;
  }

  private String sanitizeStoredFilename(String filename) {
    if (filename == null || filename.isBlank()) {
      throw new BadRequestException("Nombre de archivo inválido");
    }

    String sanitized = filename
        .replace("\\", "_")
        .replace("/", "_")
        .replaceAll("[^a-zA-Z0-9._-]", "_");

    if (sanitized.isBlank()) {
      throw new BadRequestException("Nombre de archivo inválido");
    }

    return sanitized;
  }

  private String getExtension(String filename) {
    int idx = filename.lastIndexOf('.');
    if (idx < 0 || idx == filename.length() - 1) {
      return "";
    }
    return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
  }

  private String detectMimeType(byte[] bytes) {
    if (bytes == null || bytes.length < 4) {
      return null;
    }

    if (isJpeg(bytes)) return "image/jpeg";
    if (isPng(bytes)) return "image/png";
    if (isWebp(bytes)) return "image/webp";
    if (isPdf(bytes)) return "application/pdf";

    return null;
  }

  private boolean isJpeg(byte[] bytes) {
    return bytes.length >= 3
        && (bytes[0] & 0xFF) == 0xFF
        && (bytes[1] & 0xFF) == 0xD8
        && (bytes[2] & 0xFF) == 0xFF;
  }

  private boolean isPng(byte[] bytes) {
    return bytes.length >= 8
        && (bytes[0] & 0xFF) == 0x89
        && bytes[1] == 0x50
        && bytes[2] == 0x4E
        && bytes[3] == 0x47
        && bytes[4] == 0x0D
        && bytes[5] == 0x0A
        && bytes[6] == 0x1A
        && bytes[7] == 0x0A;
  }

  private boolean isPdf(byte[] bytes) {
    return bytes.length >= 4
        && bytes[0] == 0x25
        && bytes[1] == 0x50
        && bytes[2] == 0x44
        && bytes[3] == 0x46;
  }

  private boolean isWebp(byte[] bytes) {
    return bytes.length >= 12
        && bytes[0] == 0x52
        && bytes[1] == 0x49
        && bytes[2] == 0x46
        && bytes[3] == 0x46
        && bytes[8] == 0x57
        && bytes[9] == 0x45
        && bytes[10] == 0x42
        && bytes[11] == 0x50;
  }
}