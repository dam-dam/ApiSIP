package com.upiicsa.ApiSIP.Service.Auth;

import com.upiicsa.ApiSIP.Dto.Email.ForgotPasswordDto;
import com.upiicsa.ApiSIP.Dto.Email.ResetPasswordDto;
import com.upiicsa.ApiSIP.Model.Token_Restore.TokenReset;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Token_Restore.TokenResetRepository;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import com.upiicsa.ApiSIP.Service.Infrastructure.EmailService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private UserRepository userRepository;
    private TokenResetRepository tokenResetRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;

    public PasswordResetService(UserRepository userRepository, TokenResetRepository tokenResetRepository,
                                PasswordEncoder passwordEncoder,  EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenResetRepository = tokenResetRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void createPasswordResetToken(ForgotPasswordDto request) {
        UserSIP user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró usuario con el email: " + request.email()));

        String token = UUID.randomUUID().toString();
            TokenReset tokenReset = new TokenReset(null, token, LocalDateTime.now().plusMinutes(15), null, user);
        tokenResetRepository.save(tokenReset);

        String resetUrl = "http://localhost:8080/reset-password.html?token=" + token;

        emailService.sendResetEmail(user.getEmail(), resetUrl);

        System.out.println("\n------------------------------------------------------");
        System.out.println(">>> TOKEN DE RECUPERACIÓN GENERADO: " + token);
        System.out.println(">>> ENLACE DE VALIDACIÓN ENVIADO A: " + user.getEmail());
        System.out.println(">>> URL DE VALIDACIÓN (GET): " + resetUrl);
        System.out.println("------------------------------------------------------\n");
    }

    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        Optional<TokenReset> tokenReset = tokenResetRepository.findByToken(token);

        if (tokenReset.isEmpty()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return !tokenReset.get().getExpirationDate().isBefore(now);
    }

    @Transactional
    public void resetPassword(ResetPasswordDto request) {
        TokenReset tokenReset = tokenResetRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o ya utilizado."));

        if (tokenReset.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expired token. Please Request a new.");
        }

        UserSIP user = tokenReset.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        tokenReset.setUseDate(LocalDateTime.now());
    }
}
