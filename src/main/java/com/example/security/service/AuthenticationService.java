package com.example.security.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.security.dto.LoginDto;
import com.example.security.dto.RegisterDto;
import com.example.security.dto.VerifyDto;
import com.example.security.model.User;
import com.example.security.repository.UserRepo;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
@Service
public class AuthenticationService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public User signUp(RegisterDto registerDto) {
        User user = new User(registerDto.getUsername(), registerDto.getEmail(),
                passwordEncoder.encode(registerDto.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
        sendVerificationEmail(user);
        return userRepo.save(user);
    }

    public User authenticate(LoginDto loginDto) {
        User user = userRepo.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        if (!user.isEnabled()) {
            throw new RuntimeException("User Not Verified");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        return user;
    }

    public void verifyUser(VerifyDto verifyDto) {
        Optional<User> optionalUser = userRepo.findByEmail(verifyDto.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("verification Code has expired");
            }
            if (user.getVerificationCode().equals(verifyDto.getVerificationCode())) {
                user.setEnable(true);
                user.setVerificationCode(null);
                user.setVerificationExpiration(null);
                userRepo.save(user);
            } else {
                throw new RuntimeException("Code Not Valid");
            }
        } else {
            throw new RuntimeException("User Not Verified");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("User Already Verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationExpiration(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepo.save(user);
        } else {
            throw new RuntimeException("user not found");
        }
    }

    public void sendVerificationEmail(User user) {
        String subject = "Acount verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
