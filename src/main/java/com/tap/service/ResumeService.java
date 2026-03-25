package com.tap.service;

import com.tap.model.ResumeResponse;
import com.tap.util.ResumeParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    @Autowired
    private ClaudeParserService claudeParserService;

    public ResumeResponse processResume(MultipartFile resumeFile, MultipartFile jdFile) throws Exception {

        // Step 1: Extract raw text from resume
        String resumeText = ResumeParserUtil.extractText(resumeFile);

        System.out.println("==== RESUME TEXT START ====");
        System.out.println(resumeText);
        System.out.println("==== RESUME TEXT END ====");

        // Step 2: If JD file is provided, extract its text and do matching
        if (jdFile != null && !jdFile.isEmpty()) {
            String jdText = ResumeParserUtil.extractText(jdFile);
            System.out.println("==== JD TEXT START ====");
            System.out.println(jdText);
            System.out.println("==== JD TEXT END ====");
            return claudeParserService.parseWithJD(resumeText, jdText);
        }

        // Step 3: No JD — just parse the resume
        return claudeParserService.parse(resumeText);
    }
}