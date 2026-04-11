package com.upiicsa.ApiSIP.Service.Auth;

import com.upiicsa.ApiSIP.Dto.Email.ForgotPasswordDto;
import com.upiicsa.ApiSIP.Dto.Email.ResetPasswordDto;
import com.upiicsa.ApiSIP.Exception.BusinessException;
import com.upiicsa.ApiSIP.Model.Enum.ErrorCode;
import com.upiicsa.ApiSIP.Model.Token_Restore.TokenReset;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Token_Restore.TokenResetRepository;
import com.upiicsa.ApiSIP.Service.Infrastructure.EmailService;
import com.upiicsa.ApiSIP.Service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserService userService;
    private TokenResetRepository tokenResetRepository;
    private EmailService emailService;

    public PasswordResetService(TokenResetRepository tokenResetRepository,
                                EmailService emailService, UserService userService) {
        this.tokenResetRepository = tokenResetRepository;
        this.emailService = emailService;
        this.userService = userService;
    }

    @Transactional
    public void createPasswordResetToken(ForgotPasswordDto request) {
        UserSIP user = userService.findUserByEmail(request.email());

        String token = UUID.randomUUID().toString();
            TokenReset tokenReset = new TokenReset(null, token, LocalDateTime.now().plusMinutes(15), null, user);
        tokenResetRepository.save(tokenReset);

        String resetUrl = "http://localhost:8080/reset-password.html?token=" + token;

        emailService.sendResetEmail(user.getEmail(), resetUrl);

        System.out.println(">>> TOKEN DE RECUPERACIÓN GENERADO: " + token);
        System.out.println(">>> ENLACE DE VALIDACIÓN ENVIADO A: " + user.getEmail());
    }

    @Transactional(readOnly = true)
    public boolean validatePasswordResetToken(String token) {
        TokenReset tokenReset = tokenResetRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        return !tokenReset.getExpirationDate().isBefore(now);
    }

    @Transactional
    public void resetPassword(ResetPasswordDto request) {
        TokenReset tokenReset = tokenResetRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_TOKEN));

        if (tokenReset.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        UserSIP user = tokenReset.getUser();

        userService.updatePassword(user, request.newPassword());

        tokenReset.setUseDate(LocalDateTime.now());
        tokenResetRepository.save(tokenReset);
    }

    @Transactional
    public void changePassword(Integer userId, String newPassword) {
        UserSIP user =  userService.getUserById(userId);

        if(newPassword.equals(user.getPassword())){
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        userService.updatePassword(user, newPassword);
    }
}
