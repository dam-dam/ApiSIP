package com.upiicsa.ApiSIP.Service.Auth;

import com.upiicsa.ApiSIP.Dto.Email.EmailConfirmDto;
import com.upiicsa.ApiSIP.Exception.ResourceNotFoundException;
import com.upiicsa.ApiSIP.Exception.ValidationException;
import com.upiicsa.ApiSIP.Model.Token_Restore.ConfirmationCode;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Repository.Token_Restore.ConfirmationCodeRepository;
import com.upiicsa.ApiSIP.Repository.UserRepository;
import com.upiicsa.ApiSIP.Service.Infrastructure.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailVerificationService {

    private UserRepository userRepository;
    private EmailService emailService;
    private ConfirmationCodeRepository confirmationCodeRepository;

    public EmailVerificationService (UserRepository userRepository, EmailService emailService,
                                     ConfirmationCodeRepository confirmationCodeRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.confirmationCodeRepository = confirmationCodeRepository;
    }

    @Transactional
    public void createAndSendConfirmationCode(UserSIP user) {

        String code = String.format("%06d", new Random().nextInt(1000000));

        ConfirmationCode newCode = new ConfirmationCode(null, code,
                LocalDateTime.now().plusMinutes(30), null, user);
        confirmationCodeRepository.save(newCode);

        emailService.sendConfirmationCode(user.getEmail(), code);

        System.out.println(">>> CÓDIGO DE CONFIRMACIÓN GENERADO: " + code);
        System.out.println(">>> ENVIADO A: " + user.getEmail());
    }

    @Transactional
    public void confirmEmail(EmailConfirmDto emailConfirmation) {
        var token = confirmationCodeRepository.findByCode(emailConfirmation.code())
                .orElseThrow(() -> new IllegalArgumentException("Código de confirmación inválido."));

        UserSIP user = token.getUser();

        if (user.getEmail().equals(emailConfirmation.email())) {
            if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("El código de confirmación ha expirado. Por favor, solicite uno nuevo.");
            }
            if (user.isEnabled()) {
                return;
            }
        } else {
            throw new IllegalArgumentException("El codigo no corresponde al email del usuario");
        }

        user.setEnabled(true);
        userRepository.save(user);

        token.setUseDate(LocalDateTime.now());
        confirmationCodeRepository.save(token);
    }

    @Transactional
    public void resendConfirmationCode(String email) {
        UserSIP user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con el correo: " + email));

        if (user.getEnabled()) {
            throw new ValidationException("Este usuario ya ha sido verificado anteriormente.");
        }

        createAndSendConfirmationCode(user);
    }
}