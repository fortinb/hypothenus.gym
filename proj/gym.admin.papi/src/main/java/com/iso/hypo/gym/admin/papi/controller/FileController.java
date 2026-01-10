package com.iso.hypo.gym.admin.papi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.iso.hypo.gym.domain.services.FileService;

@RestController
@RequestMapping("/v1")
@Validated
public class FileController {

	private FileService fileService;

	public FileController(FileService fileService) {
		this.fileService = fileService;
	}

	@PostMapping("/files/images/upload")
	public ResponseEntity<String> handlePhotoUpload(@RequestParam("file") MultipartFile file,
			@RequestParam("metadata") String metadata,
			RedirectAttributes redirectAttributes) {

		String fileUrl = fileService.storeImage(metadata, file);

		return ResponseEntity.ok(fileUrl);
	}
}
