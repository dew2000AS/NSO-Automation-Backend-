package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.RegisterRequest;
import com.example.SPSProjectBackend.model.User;
import com.example.SPSProjectBackend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUser(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByNicno(String nicno) {
        return userRepository.findByNicno(nicno)
                .orElseThrow(() -> new RuntimeException("User not found with NIC: " + nicno));
    }

    public User addUser(RegisterRequest registerRequest) {
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setEAccountNo(registerRequest.getEAccountNo());

        // Encode the password using BCrypt
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Generate a verification code and save it
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);

        // Set the user as not verified initially
        user.setVerified(false);

        // Send the verification email (email logic will be implemented separately)
        sendVerificationEmail(user);

        return userRepository.save(user);  // Save the new user
    }

    private String generateVerificationCode() {
        // You can use a random UUID or a custom code generator here
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    @Autowired
    private JavaMailSender mailSender;

    private void sendVerificationEmail(User user) {
        // Logic to send an email with the verification link containing the verification code
        String verificationLink = "http://127.0.0.1:8088/SPSProjectBackend-0.0.1-SNAPSHOT/api/v1/verify?code=" + user.getVerificationCode();

        // Create a simple email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Email Verification");
        message.setText("Hello " + user.getName() + ",\n\n"
                + "Please click the link below to verify your email address:\n"
                + verificationLink + "\n\n"
                + "Thank you!");
        mailSender.send(message);
    }

    public User updateUser(User user) {
        user.setPassword(hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

//    public String authenticate(String email, String password, HttpSession session) {
//        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User does not exist in the database"));
//
//
//        if (user == null) {
//            throw new RuntimeException("User does not exist in the database");
//        }
//
//
//        if (!user.isVerified()) {
//            throw new RuntimeException("Email not verified. Please check your inbox.");
//        }
//
//
//        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
//            throw new RuntimeException("The password is incorrect");
//        }
//
//
//        // Store user details in session
//        session.setAttribute("userId", user.getId());
//        session.setAttribute("username", user.getEmail());
//        session.setAttribute("userLevel", user.getUserlevel());
//        session.setAttribute("eAccountNo", user.getEAccountNo());
//
//
//        // Set session timeout to 1 minute (60 seconds)
//        session.setMaxInactiveInterval(600000000);
//
//
    ////        return true;
//        return user.getUserlevel();
//    }

    public User authenticate(String email, String password, HttpSession session) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist in the database"));

        if (!user.isVerified()) {
            throw new RuntimeException("Email not verified. Please check your inbox.");
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("The password is incorrect");
        }

        session.setAttribute("email", user.getEmail());
        session.setAttribute("userLevel", user.getUserlevel());
        session.setAttribute("deptId", user.getDeptId());
        session.setAttribute("eAccountNo", user.getEAccountNo());
        session.setMaxInactiveInterval(600); // 10 minutes

        System.out.println("DeptId set in session: " + user.getDeptId());

        return user;
    }




    public String logout(HttpSession session) {
        session.invalidate();
        return "User logged out, session invalidated!";
    }


    public Optional<User> verifyEmail(String verificationCode) {
        Optional<User> userOpt = userRepository.findByVerificationCode(verificationCode);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if the user is not already verified
            if (!user.isVerified()) {
                user.setVerified(true);
                user.setEnabled(true);  // Enable the user after verification
                userRepository.save(user);
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    private String hashPassword(String password) {
        // Implement your password hashing logic here
        return password; // Replace with actual hashing
    }

    /// /// /// Password Reset Logic /// ///


    @Autowired
    private EmailService emailService;

    private final Map<String, String> otpStorage = new HashMap<>(); // Temporary OTP storage

    public void sendOtp(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User with this email does not exist.");
        }

        String otp = String.valueOf(new Random().nextInt(999999)); // Generate 6-digit OTP
        otpStorage.put(email, otp);
        // Send OTP via email
        emailService.sendEmail(email, "Password Reset OTP", "Your OTP is: " + otp);
    }

    public boolean verifyOtp(String email, String otp) {
        return otp.equals(otpStorage.get(email));
    }
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User with this email does not exist.");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword)); // Hash the password before saving
        userRepository.save(user);

        otpStorage.remove(email); // Clear OTP after successful reset
    }
}