package com.reparasuite.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.util.ReflectionTestUtils;

class SecureUploadServiceContentTypeTest {

  @TempDir
  Path tempDir;

  @Test
  void debeResolverContentTypePorExtensionSiProbeFalla() throws IOException {
    SecureUploadService service = new SecureUploadService();
    ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());

    Path pdf = tempDir.resolve("archivo.pdf");
    Files.write(pdf, new byte[] { 0x25, 0x50, 0x44, 0x46 });

    Resource resource = new UrlResource(pdf.toUri());
    String contentType = service.probeContentType(resource, "archivo.pdf");

    assertEquals("application/pdf", contentType);
  }
}