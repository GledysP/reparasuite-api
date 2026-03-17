package com.reparasuite.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.reparasuite.api.exception.BadRequestException;

class SecureUploadServiceTest {

  @TempDir
  Path tempDir;

  private SecureUploadService secureUploadService;

  @BeforeEach
  void setUp() {
    secureUploadService = new SecureUploadService();
    ReflectionTestUtils.setField(secureUploadService, "uploadDir", tempDir.toString());
  }

  @Test
  void debeGuardarImagenOtValida() throws IOException {
    byte[] jpg = new byte[] {
        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
        0x00, 0x10, 0x4A, 0x46, 0x49, 0x46
    };

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "foto.jpg",
        "image/jpeg",
        jpg
    );

    UUID otId = UUID.randomUUID();
    SecureUploadService.StoredFile stored = secureUploadService.storeOtImage(otId, file);

    assertNotNull(stored);
    assertEquals("foto.jpg", stored.originalFilename());
    assertEquals("image/jpeg", stored.contentType());

    Path expectedDir = tempDir.resolve("ot").resolve(otId.toString());
    assertEquals(true, Files.exists(expectedDir));
  }

  @Test
  void debeRechazarExtensionNoPermitida() {
    byte[] fakeExe = new byte[] { 0x01, 0x02, 0x03, 0x04 };

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "malicioso.exe",
        "application/octet-stream",
        fakeExe
    );

    assertThrows(BadRequestException.class, () ->
        secureUploadService.storeOtImage(UUID.randomUUID(), file)
    );
  }

  @Test
  void debeRechazarTipoRealNoPermitidoAunqueLaExtensionSeaJpg() {
    byte[] fakePdf = new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D };

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "enganoso.jpg",
        "image/jpeg",
        fakePdf
    );

    assertThrows(BadRequestException.class, () ->
        secureUploadService.storeOtImage(UUID.randomUUID(), file)
    );
  }

  @Test
  void debeGuardarComprobantePdfValido() throws IOException {
    byte[] pdf = new byte[] { 0x25, 0x50, 0x44, 0x46, 0x2D };

    MockMultipartFile file = new MockMultipartFile(
        "file",
        "comprobante.pdf",
        "application/pdf",
        pdf
    );

    UUID otId = UUID.randomUUID();
    SecureUploadService.StoredFile stored = secureUploadService.storePaymentReceipt(otId, file);

    assertNotNull(stored);
    assertEquals("application/pdf", stored.contentType());
  }
}