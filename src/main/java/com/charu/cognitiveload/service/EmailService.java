package com.charu.cognitiveload.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Generate 6 digit OTP
    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Send OTP email
    public void sendOTPEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CogniLoad — Your Verification Code");
        message.setText(
                "Hello!\n\n" +
                        "Your verification code is: " + otp + "\n\n" +
                        "This code expires in 10 minutes.\n\n" +
                        "If you didn't request this, ignore this email.\n\n" +
                        "— CogniLoad Team"
        );
        mailSender.send(message);
    }
}