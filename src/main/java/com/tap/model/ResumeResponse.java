package com.tap.model;

import java.util.List;

public class ResumeResponse {
    private String name;
    private String email;
    private String phone;
    private String linkedin;
    private String github;
    private String role;
    private double experienceYears;
    private List<String> skills;
    private List<String> languages;
    private List<String> education;
    private List<String> experience;
    private List<String> projects;
    private List<String> certifications;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private int eligibilityScore;
    private String eligibilityVerdict;

    public ResumeResponse() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getExperienceYears() { return experienceYears; }
    public void setExperienceYears(double experienceYears) { this.experienceYears = experienceYears; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }

    public List<String> getExperience() { return experience; }
    public void setExperience(List<String> experience) { this.experience = experience; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public List<String> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<String> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public int getEligibilityScore() { return eligibilityScore; }
    public void setEligibilityScore(int eligibilityScore) { this.eligibilityScore = eligibilityScore; }

    public String getEligibilityVerdict() { return eligibilityVerdict; }
    public void setEligibilityVerdict(String eligibilityVerdict) { this.eligibilityVerdict = eligibilityVerdict; }
}