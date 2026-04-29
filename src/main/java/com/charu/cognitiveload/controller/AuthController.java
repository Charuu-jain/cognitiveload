package com.charu.cognitiveload.controller;

import com.charu.cognitiveload.model.User;
import com.charu.cognitiveload.repository.UserRepository;
import com.charu.cognitiveload.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired UserRepository userRepository;
    @Autowired EmailService emailService;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/signup")
    public String signupPage() { return "signup"; }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error",
                    "Email already registered!");
            return "signup";
        }


        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setOtpVerified(true);


        String otp = emailService.generateOTP();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        try {
            emailService.sendOTPEmail(email, otp);
        } catch (Exception e) {
            model.addAttribute("error",
                    "Could not send OTP. Check email config.");
            return "signup";
        }


        session.setAttribute("pendingEmail", email);
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(
            HttpSession session, Model model) {
        if (session.getAttribute("pendingEmail") == null) {
            return "redirect:/signup";
        }
        model.addAttribute("email",
                session.getAttribute("pendingEmail"));
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @RequestParam String otp,
            HttpSession session,
            Model model) {

        String email = (String) session
                .getAttribute("pendingEmail");
        if (email == null) return "redirect:/signup";

        Optional<User> userOpt =
                userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return "redirect:/signup";

        User user = userOpt.get();


        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            model.addAttribute("error",
                    "OTP expired! Please signup again.");
            model.addAttribute("email", email);
            return "verify-otp";
        }


        if (!user.getOtp().equals(otp.trim())) {
            model.addAttribute("error", "Wrong OTP!");
            model.addAttribute("email", email);
            return "verify-otp";
        }


        user.setOtpVerified(true);
        user.setOtp(null);
        userRepository.save(user);
        session.removeAttribute("pendingEmail");

        return "redirect:/login?verified";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<User> userOpt =
                userRepository.findByEmail(email);

        if (userOpt.isEmpty() || !encoder.matches(
                password, userOpt.get().getPassword())) {
            model.addAttribute("error",
                    "Invalid email or password!");
            return "login";
        }

        User user = userOpt.get();

        if (!user.getOtpVerified()) {
            model.addAttribute("error",
                    "Please verify your email first!");
            return "login";
        }

        session.setAttribute("user", user);
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}