package com.sahasathi.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirebaseService {

    public FirebaseToken verifyIdToken(String idToken) {
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);
            log.debug("Firebase token verified for UID: {}", decoded.getUid());
            return decoded;
        } catch (FirebaseAuthException e) {
            log.error("Firebase token verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired Firebase token", e);
        }
    }

    public String getPhoneNumberFromToken(String idToken) {
        FirebaseToken decoded = verifyIdToken(idToken);
        String phone = decoded.getClaims().get("phone_number").toString();
        if (phone == null || phone.isBlank()) {
            throw new RuntimeException("Phone number not found in Firebase token");
        }
        return phone;
    }
}
