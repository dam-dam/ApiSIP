package com.upiicsa.ApiSIP.Service.Infrastructure;

import com.upiicsa.ApiSIP.Exception.BusinessException;
import com.upiicsa.ApiSIP.Model.Enum.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${SPRING_EMAIL_USERNAME}")
    private String email;

    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String toEmail, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(email);
        message.setTo(toEmail);
        message.setSubject("Solicitud de Restablecimiento de Contraseña");

        String emailContent = "Estimado usuario,\n\n"
                + "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.\n"
                + "Por favor, haz clic en el siguiente enlace para continuar. Este enlace expira en 60 minutos.\n\n"
                + "Enlace de Restablecimiento: " + resetUrl + "\n\n"
                + "Si no solicitaste este cambio, puedes ignorar este correo.\n\n"
                + "Atentamente,\n"
                + "Tu equipo de Soporte.";
        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public void sendConfirmationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(email);
        message.setTo(toEmail);
        message.setSubject("Verificación de Correo Electrónico");

        String emailContent = "¡Bienvenido a nuestra plataforma!\n\n"
                + "Tu registro está casi completo. Por favor, utiliza el siguiente código para verificar tu dirección de correo electrónico:\n\n"
                + "Código de Verificación: " + code + "\n\n"
                + "Este código expira en 15 minutos.\n\n"
                + "Atentamente,\n"
                + "Tu equipo de Soporte.";

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
