package de.unipassau.allocationsystem.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/")
public class OpenApiController {

    @GetMapping(path = "openapi.yaml", produces = "application/x-yaml")
    public ResponseEntity<String> getOpenApiYaml() throws IOException {
        Resource resource = new ClassPathResource("openapi.yaml");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = resource.getInputStream().readAllBytes();
        String yaml = new String(bytes, StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-yaml"));
        return ResponseEntity.ok().headers(headers).body(yaml);
    }
}
