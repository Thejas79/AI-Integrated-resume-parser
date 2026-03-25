package com.tap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tap.model.ResumeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClaudeParserService {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile";

    private static final String SYSTEM_PROMPT_PARSE_ONLY = """
        You are an expert resume parser and career analyst.
        Extract structured information from the raw resume text provided.

        Return ONLY a valid JSON object with exactly these fields (no markdown, no extra text):
        {
          "name": "string",
          "email": "string",
          "phone": "string",
          "linkedin": "string",
          "github": "string",
          "role": "string",
          "experienceYears": number,
          "skills": ["string"],
          "languages": ["string"],
          "education": ["string"],
          "experience": ["string"],
          "projects": ["string"],
          "certifications": ["string"],
          "matchedSkills": [],
          "missingSkills": [],
          "eligibilityScore": 0,
          "eligibilityVerdict": "Not evaluated"
        }

        Rules:
        - "role" must be the most suitable job role based on the candidate's skills and experience.
        - "experienceYears" must be a number. Calculate total years from all experience dates. Return 0 for freshers.
        - "languages" must contain only PROGRAMMING languages, not human languages.
        - "certifications" must contain only certification names, no URLs.
        - "projects" should list project name with a one-line description each.
        - "experience" should list each role as: "Job Title | Company | Duration".
        - Do NOT include any URLs, mailto links, or hyperlinks in any field.
        - Return raw JSON only. No ```json fences. No explanation.
        """;

    private static final String SYSTEM_PROMPT_WITH_JD = """
        You are an expert resume screener and HR analyst.
        You will receive a candidate resume and a job requirement/description.
        Analyze both carefully and return ONLY a valid JSON object (no markdown, no extra text):
        {
          "name": "string",
          "email": "string",
          "phone": "string",
          "linkedin": "string",
          "github": "string",
          "role": "string",
          "experienceYears": number,
          "skills": ["string"],
          "languages": ["string"],
          "education": ["string"],
          "experience": ["string"],
          "projects": ["string"],
          "certifications": ["string"],
          "matchedSkills": ["string"],
          "missingSkills": ["string"],
          "eligibilityScore": number,
          "eligibilityVerdict": "string"
        }

        Rules:
        - "role" must reflect the most suitable role based on resume AND job description.
        - "experienceYears" must be a number. Calculate total years from all experience dates. Return 0 for freshers.
        - "languages" must contain only PROGRAMMING languages, not human languages.
        - "matchedSkills" must list skills present in BOTH the resume AND the job requirement.
        - "missingSkills" must list skills required in the job description that are NOT in the resume.
        - "eligibilityScore" is a number 0-100 based on: skill match, experience years, role alignment, certifications.
        - "eligibilityVerdict" must be exactly one of:
            "Eligible" if score >= 75
            "Partially Eligible" if score >= 40
            "Not Eligible" if score < 40
        - "certifications" must contain only certification names, no URLs.
        - "projects" should list project name with a one-line description each.
        - "experience" should list each role as: "Job Title | Company | Duration".
        - Do NOT include any URLs, mailto links, or hyperlinks in any field.
        - Return raw JSON only. No ```json fences. No explanation.
        """;

    public ResumeResponse parse(String resumeText) throws Exception {
        return callGroq(SYSTEM_PROMPT_PARSE_ONLY, "Parse this resume:\n\n" + resumeText);
    }

    public ResumeResponse parseWithJD(String resumeText, String jdText) throws Exception {
        String userMessage = "RESUME:\n" + resumeText + "\n\n---\n\nJOB REQUIREMENT:\n" + jdText;
        return callGroq(SYSTEM_PROMPT_WITH_JD, userMessage);
    }

    private ResumeResponse callGroq(String systemPrompt, String userMessage) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String requestBody = """
            {
              "model": "%s",
              "messages": [
                { "role": "system", "content": %s },
                { "role": "user",   "content": %s }
              ],
              "temperature": 0,
              "max_tokens": 2048
            }
            """.formatted(
                MODEL,
                mapper.writeValueAsString(systemPrompt),
                mapper.writeValueAsString(userMessage)
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Groq API error: " + response.statusCode() + " - " + response.body());
        }

        return parseGroqResponse(response.body(), mapper);
    }

    private ResumeResponse parseGroqResponse(String responseBody, ObjectMapper mapper) throws Exception {
        JsonNode root = mapper.readTree(responseBody);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        content = content.replaceAll("```json", "").replaceAll("```", "").trim();

        JsonNode p = mapper.readTree(content);

        ResumeResponse res = new ResumeResponse();
        res.setName(p.path("name").asText(""));
        res.setEmail(p.path("email").asText(""));
        res.setPhone(p.path("phone").asText(""));
        res.setLinkedin(p.path("linkedin").asText(""));
        res.setGithub(p.path("github").asText(""));
        res.setRole(p.path("role").asText(""));
        res.setExperienceYears(p.path("experienceYears").asDouble(0));
        res.setSkills(toList(p.path("skills")));
        res.setLanguages(toList(p.path("languages")));
        res.setEducation(toList(p.path("education")));
        res.setExperience(toList(p.path("experience")));
        res.setProjects(toList(p.path("projects")));
        res.setCertifications(toList(p.path("certifications")));
        res.setMatchedSkills(toList(p.path("matchedSkills")));
        res.setMissingSkills(toList(p.path("missingSkills")));
        res.setEligibilityScore(p.path("eligibilityScore").asInt(0));
        res.setEligibilityVerdict(p.path("eligibilityVerdict").asText("Not evaluated"));
        return res;
    }

    private List<String> toList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                String val = item.asText("").trim();
                if (!val.isEmpty()) list.add(val);
            }
        }
        return list;
    }
}