package com.isoceles.hypothenus.gym.domain.services;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

//import com.isoceles.hypothenus.gym.domain.context.RequestContext;

@Service
public class FileService {

//	@Autowired
	//private RequestContext requestContext;
	
	public FileService() {
	}

	public String storeImage(String metadata, MultipartFile file) {
		// Implement your file storage logic here
		// For example, save the file to a local directory or cloud storage
		// Return the URL or path of the stored file
		String fileUrl = "http://localhost/files/" + file.getOriginalFilename();
		return fileUrl;
	}
}
