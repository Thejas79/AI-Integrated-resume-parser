package com.tap.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.tap.model.User;
import com.tap.repository.UserRepository;
import com.tap.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "https://benevolent-sunburst-76e62d.netlify.app")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${google.client.id}")
    private String googleClientId;

    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("idToken");

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(List.of(googleClientId))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
            }

            GoogleIdToken.Payload payload = token.getPayload();
            String googleId = payload.getSubject();

            // Upsert user
            User user = userRepo.findByGoogleId(googleId).orElseGet(() -> {
                User u = new User();
                u.setGoogleId(googleId);
                u.setEmail(payload.getEmail());
                u.setName((String) payload.get("name"));
                u.setPicture((String) payload.get("picture"));
                return u;
            });
            userRepo.save(user);

            String jwt = jwtUtil.generateToken(user.getId());

            return ResponseEntity.ok(Map.of(
                    "token",      jwt,
                    "name",       user.getName() != null ? user.getName() : "",
                    "email",      user.getEmail(),
                    "picture",    user.getPicture() != null ? user.getPicture() : "",
                    "freeChecks", user.getFreeChecks(),
                    "paidChecks", user.getPaidChecks()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String bearer) {
        try {
            Long userId = jwtUtil.getUserId(bearer);
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(Map.of(
                    "name",       user.getName() != null ? user.getName() : "",
                    "email",      user.getEmail(),
                    "picture",    user.getPicture() != null ? user.getPicture() : "",
                    "freeChecks", user.getFreeChecks(),
                    "paidChecks", user.getPaidChecks()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
    }
}