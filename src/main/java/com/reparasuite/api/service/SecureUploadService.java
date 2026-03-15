package com.reparasuite.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SecureUploadService {

  private static final long MAX_BYTES = 10L * 1024L * 1024L;

  private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");
  private static final Set<String> RECEIPT_EXT = Set.of("jpg", "jpeg", "png", "webp", "pdf");

  @Value("${reparasuite.upload-dir}")
  private String uploadDir;

  public StoredUpload storeOtImage(UUID otId, MultipartFile file) throws IOException {
    ValidatedFile valid = validate(file, FileKind.IMAGE);
    String filename = "ot-" + otId + "-" + UUID.randomUUID() + "-" + valid.safeOriginalName();
    Path base = baseDir();
    Files.createDirectories(base);

    Path target = base.resolve(filename).normalize();
    ensureInside(base, target);

    Files.write(target, valid.bytes(), StandardOpenOption.CREATE_NEW);
    return new StoredUpload("/files/" + filename, valid.originalName(), valid.detectedMime());
  }

  public StoredUpload storePaymentReceipt(UUID otId, MultipartFile file) throws IOException {
    ValidatedFile valid = validate(file, FileKind.RECEIPT);
    String filename = "pago-" + otId + "-" + UUID.randomUUID() + "-" + valid.safeOriginalName();
    Path base = baseDir();
    Files.createDirectories(base);

    Path target = base.resolve(filename).normalize();
    ensureInside(base, target);

    Files.write(target, valid.bytes(), StandardOpenOption.CREATE_NEW);
    return new StoredUpload("/files/" + filename, valid.originalName(), valid.detectedMime());
  }

  public StoredUpload storeTicketImage(UUID ticketId, MultipartFile file) throws IOException {
    ValidatedFile valid = validate(file, FileKind.IMAGE);
    String filename = UUID.randomUUID() + "-" + valid.safeOriginalName();

    Path base = baseDir();
    Path ticketDir = base.resolve("tickets").resolve(ticketId.toString()).normalize();

    Files.createDirectories(ticketDir);
    ensureInside(base, ticketDir);

    Path target = ticketDir.resolve(filename).normalize();
    ensureInside(base, target);

    Files.write(target, valid.bytes(), StandardOpenOption.CREATE_NEW);
    return new StoredUpload("/files/tickets/" + ticketId + "/" + filename, valid.originalName(), valid.detectedMime());
  }

  public void deleteByUrl(String url) {
    if (url == null || url.isBlank()) return;

    String normalized = url.trim();
    if (!normalized.startsWith("/files/")) return;

    String relative = normalized.substring("/files/".length());
    if (relative.isBlank()) return;

    Path base = baseDir();

    try {
      Path target = base.resolve(relative).normalize();
      ensureInside(base, target);
      Files.deleteIfExists(target);
      cleanupEmptyParents(base, target.getParent());
    } catch (Exception ignored) {
    }
  }

  private void cleanupEmptyParents(Path base, Path current) {
    try {
      while (current != null && current.startsWith(base) && !current.equals(base)) {
        if (!Files.exists(current)) {
          current = current.getParent();
          continue;
        }

        try (var stream = Files.list(current)) {
          if (stream.findAny().isPresent()) {
            break;
          }
        }

        Files.deleteIfExists(current);
        current = current.getParent();
      }
    } catch (Exception ignored) {
    }
  }

  private Path baseDir() {
    return Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  private ValidatedFile validate(MultipartFile file, FileKind kind) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Archivo vacío");
    }

    byte[] bytes = file.getBytes();
    if (bytes.length == 0) {
      throw new IllegalArgumentException("Archivo vacío");
    }

    if (bytes.length > MAX_BYTES) {
      throw new IllegalArgumentException("El archivo supera el tamaño máximo permitido");
    }

    String originalName = file.getOriginalFilename() == null ? "archivo" : file.getOriginalFilename().trim();
    String ext = extensionOf(originalName);

    Set<String> allowedExt = kind == FileKind.IMAGE ? IMAGE_EXT : RECEIPT_EXT;
    if (!allowedExt.contains(ext)) {
      throw new IllegalArgumentException("Extensión de archivo no permitida");
    }

    String mime = detectMime(bytes);
    if (mime == null) {
      throw new IllegalArgumentException("No se pudo validar el tipo real del archivo");
    }

    if (kind == FileKind.IMAGE && !isAllowedImageMime(mime)) {
      throw new IllegalArgumentException("Solo se permiten imágenes JPEG, PNG o WEBP");
    }

    if (kind == FileKind.RECEIPT && !isAllowedReceiptMime(mime)) {
      throw new IllegalArgumentException("Solo se permiten PDF o imágenes JPEG, PNG o WEBP");
    }

    return new ValidatedFile(originalName, safeName(originalName), ext, mime, bytes);
  }

  private void ensureInside(Path base, Path target) {
    if (!target.startsWith(base)) {
      throw new SecurityException("Ruta de archivo no permitida");
    }
  }

  private boolean isAllowedImageMime(String mime) {
    return "image/jpeg".equals(mime)
        || "image/png".equals(mime)
        || "image/webp".equals(mime);
  }

  private boolean isAllowedReceiptMime(String mime) {
    return isAllowedImageMime(mime) || "application/pdf".equals(mime);
  }

  private String extensionOf(String name) {
    int idx = name.lastIndexOf('.');
    if (idx < 0 || idx == name.length() - 1) {
      return "";
    }
    return name.substring(idx + 1).toLowerCase(Locale.ROOT);
  }

  private String safeName(String n) {
    String cleaned = n == null || n.isBlank() ? "archivo.bin" : n;
    return cleaned.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  private String detectMime(byte[] bytes) {
    if (isPdf(bytes)) return "application/pdf";
    if (isPng(bytes)) return "image/png";
    if (isJpeg(bytes)) return "image/jpeg";
    if (isWebp(bytes)) return "image/webp";
    return null;
  }

  private boolean isPdf(byte[] bytes) {
    return bytes.length >= 4
        && bytes[0] == 0x25
        && bytes[1] == 0x50
        && bytes[2] == 0x44
        && bytes[3] == 0x46;
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

  private boolean isJpeg(byte[] bytes) {
    return bytes.length >= 3
        && (bytes[0] & 0xFF) == 0xFF
        && (bytes[1] & 0xFF) == 0xD8
        && (bytes[2] & 0xFF) == 0xFF;
  }

  private boolean isWebp(byte[] bytes) {
    return bytes.length >= 12
        && bytes[0] == 'R'
        && bytes[1] == 'I'
        && bytes[2] == 'F'
        && bytes[3] == 'F'
        && bytes[8] == 'W'
        && bytes[9] == 'E'
        && bytes[10] == 'B'
        && bytes[11] == 'P';
  }

  private enum FileKind {
    IMAGE,
    RECEIPT
  }

  public record StoredUpload(
      String url,
      String originalFilename,
      String contentType
  ) {}

  private record ValidatedFile(
      String originalName,
      String safeOriginalName,
      String extension,
      String detectedMime,
      byte[] bytes
  ) {}
}