package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.responses.BackupResponse;
import com.jhonatan.sibv2.dto.responses.MensagemResponse;
import com.jhonatan.sibv2.services.BackupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backup")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class BackupController {
    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BackupResponse criarBackup() {
        return backupService.criarBackup();
    }

    @PostMapping(value = "/restaurar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MensagemResponse restaurarBackup(@RequestParam("arquivo") MultipartFile arquivo) {
        backupService.restaurarBackup(arquivo);
        return new MensagemResponse("Backup restaurado. Entre novamente no sistema.");
    }
}
