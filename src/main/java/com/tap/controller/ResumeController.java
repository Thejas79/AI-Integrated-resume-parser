package com.tap.controller;

import com.tap.model.User;
import com.tap.repository.UserRepository;
import com.tap.service.ResumeService;
import com.tap.model.ResumeResponse;
import com.tap.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired private ResumeService resumeService;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    private static final java.util.Set<String> UNLIMITED_EMAILS = java.util.Set.of(
    	    "tthejaskumar79@gmail.com"
    	);

    	@PostMapping("/parse")
    	public ResumeResponse parseResume(
    	        @RequestHeader("Authorization") String bearer,
    	        @RequestParam("file") MultipartFile resumeFile,
    	        @RequestParam(value = "jd", required = false) MultipartFile jdFile
    	) throws Exception {

    	    Long userId = jwtUtil.getUserId(bearer);
    	    User user = userRepo.findById(userId)
    	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    	    // ✅ WHITELIST — skip all quota checks for this account
    	    boolean isUnlimited = UNLIMITED_EMAILS.contains(user.getEmail().toLowerCase());

    	    if (!isUnlimited) {
    	        // Check quota — paid checks used first, then free
    	        if (user.getPaidChecks() <= 0 && user.getFreeChecks() <= 0) {
    	            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
    	                    "No checks remaining. Purchase more to continue.");
    	        }

    	        // Deduct one check
    	        if (user.getPaidChecks() > 0) {
    	            user.setPaidChecks(user.getPaidChecks() - 1);
    	        } else {
    	            user.setFreeChecks(user.getFreeChecks() - 1);
    	        }
    	        userRepo.save(user);
    	    }

    	    return resumeService.processResume(resumeFile, jdFile);
    	}
}