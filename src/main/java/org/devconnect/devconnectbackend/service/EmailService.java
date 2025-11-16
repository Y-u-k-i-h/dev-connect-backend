package org.devconnect.devconnectbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Value("${MAIL_FROM}")
    private String fromEmail;

    public void sendHtmlEmail(String toWho, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toWho);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email: " + e.getMessage(), e);
        }
    }

    public void sendAccountVerificationEmail(String toEmail, String verificationCode) {

    String subject = "DevConnect Account Verification";

    String body = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verify Your Account</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); min-height: 100vh;">
                <table role="presentation" cellpadding="0" cellspacing="0" style="width: 100%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 40px 20px;">
                            <table role="presentation" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3); overflow: hidden;">
            
                                <!-- Header with gradient -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #6B46C1 0%, #9333EA 50%, #7C3AED 100%); padding: 40px 30px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                            DevConnect
                                        </h1>
                                    </td>
                                </tr>
            
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="margin: 0 0 16px 0; color: #1a1a2e; font-size: 24px; font-weight: 600;">
                                            Welcome to DevConnect! 🎉
                                        </h2>
            
                                        <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                            Hi there,
                                        </p>
            
                                        <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                            Thanks for signing up! We're excited to have you on board. To get started, please verify your email address using the code below:
                                        </p>
            
                                        <!-- Verification Code Box -->
                                        <div style="margin: 30px 0;">
                                            <div style="background: linear-gradient(135deg, #6B46C1 0%, #9333EA 50%, #7C3AED 100%); border-radius: 12px; padding: 3px;">
                                                <div style="background: #ffffff; border-radius: 10px; padding: 24px; text-align: center;">
                                                    <p style="margin: 0 0 8px 0; color: #718096; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;">
                                                        Your Verification Code
                                                    </p>
                                                    <div style="font-family: 'Courier New', monospace; font-size: 36px; font-weight: 700; color: #6B46C1; letter-spacing: 8px; margin: 0;">
                                                        {{VERIFICATION_CODE}}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
            
                                        <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 14px; line-height: 1.6;">
                                            This code will expire in <strong>30 minutes</strong> for security reasons.
                                        </p>
            
                                        <div style="background: #f0f9ff; border-left: 4px solid #7C3AED; border-radius: 6px; padding: 16px; margin: 20px 0;">
                                            <p style="margin: 0; color: #1e40af; font-size: 14px; line-height: 1.6;">
                                                <strong>💡 Quick Tip:</strong> Once verified, you'll have full access to connect with developers, share projects, and collaborate with the community!
                                            </p>
                                        </div>
            
                                        <p style="margin: 0; color: #4a5568; font-size: 14px; line-height: 1.6;">
                                            If you didn't create an account with DevConnect, you can safely ignore this email.
                                        </p>
                                    </td>
                                </tr>
            
                                <!-- Footer -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%); padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                        <p style="margin: 0 0 10px 0; color: #718096; font-size: 14px;">
                                            Welcome aboard,<br>
                                            <strong style="color: #6B46C1;">The DevConnect Team</strong>
                                        </p>
            
                                        <p style="margin: 20px 0 0 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                            Need help? Contact us at <a href="mailto:support@devconnect.com" style="color: #7C3AED; text-decoration: none;">support@devconnect.com</a>
                                        </p>
                                    </td>
                                </tr>
            
                            </table>
            
                            <!-- Disclaimer -->
                            <table role="presentation" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 20px auto 0;">
                                <tr>
                                    <td style="text-align: center; padding: 0 20px;">
                                        <p style="margin: 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                            This email was sent because an account was created with this email address on DevConnect.
                                        </p>
                                    </td>
                                </tr>
                            </table>
            
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.replace("{{VERIFICATION_CODE}}", verificationCode);

    sendHtmlEmail(toEmail, subject, body);
}

    public void sendPasswordRestEmail(String toEmail, String resetCode) {
        String subject = "DevConnect Password Reset Request";

        String body = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset Request</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); min-height: 100vh;">
                    <table role="presentation" cellpadding="0" cellspacing="0" style="width: 100%; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 40px 20px;">
                                <table role="presentation" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3); overflow: hidden;">
                
                                    <!-- Header with gradient -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #6B46C1 0%, #9333EA 50%, #7C3AED 100%); padding: 40px 30px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                                DevConnect
                                            </h1>
                                        </td>
                                    </tr>
                
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <h2 style="margin: 0 0 16px 0; color: #1a1a2e; font-size: 24px; font-weight: 600;">
                                                Password Reset Request
                                            </h2>
                
                                            <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                Hello,
                                            </p>
                
                                            <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                                We received a request to reset your password. Use the verification code below to complete the process:
                                            </p>
                
                                            <!-- Verification Code Box -->
                                            <div style="margin: 30px 0;">
                                                <div style="background: linear-gradient(135deg, #6B46C1 0%, #9333EA 50%, #7C3AED 100%); border-radius: 12px; padding: 3px;">
                                                    <div style="background: #ffffff; border-radius: 10px; padding: 24px; text-align: center;">
                                                        <p style="margin: 0 0 8px 0; color: #718096; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;">
                                                            Your Verification Code
                                                        </p>
                                                        <div style="font-family: 'Courier New', monospace; font-size: 36px; font-weight: 700; color: #6B46C1; letter-spacing: 8px; margin: 0;">
                                                            {{RESET_CODE}}
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                
                                            <p style="margin: 0 0 10px 0; color: #4a5568; font-size: 14px; line-height: 1.6;">
                                                This code will expire in <strong>5 minutes</strong> for security reasons.
                                            </p>
                
                                            <p style="margin: 0; color: #4a5568; font-size: 14px; line-height: 1.6;">
                                                If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.
                                            </p>
                                        </td>
                                    </tr>
                
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%); padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                            <p style="margin: 0 0 10px 0; color: #718096; font-size: 14px;">
                                                Best regards,<br>
                                                <strong style="color: #6B46C1;">The DevConnect Team</strong>
                                            </p>
                
                                            <p style="margin: 20px 0 0 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                If you have any questions, please contact us at <a href="mailto:support@devconnect.com" style="color: #7C3AED; text-decoration: none;">support@devconnect.com</a>
                                            </p>
                                        </td>
                                    </tr>
                
                                </table>
                
                                <!-- Disclaimer -->
                                <table role="presentation" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 20px auto 0;">
                                    <tr>
                                        <td style="text-align: center; padding: 0 20px;">
                                            <p style="margin: 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                                This email was sent to you because a password reset was requested for your DevConnect account.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.replace("{{RESET_CODE}}", resetCode);

        sendHtmlEmail(toEmail, subject, body);
    }

    public void sendPasswordResetSuccessEmail(String toEmail) {
    String subject = "DevConnect Password Reset Successful";

    String body = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset Successful</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); min-height: 100vh;">
                <table role="presentation" cellpadding="0" cellspacing="0" style="width: 100%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 40px 20px;">
                            <table role="presentation" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3); overflow: hidden;">
                                
                                <!-- Header with gradient -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #6B46C1 0%, #9333EA 50%, #7C3AED 100%); padding: 40px 30px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                            DevConnect
                                        </h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <!-- Success Icon -->
                                        <div style="text-align: center; margin-bottom: 24px;">
                                            <div style="display: inline-block; width: 80px; height: 80px; background: linear-gradient(135deg, #6B46C1 0%, #9333EA 50%, #7C3AED 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center;">
                                                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" style="vertical-align: middle;">
                                                    <path d="M20 6L9 17L4 12" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
                                                </svg>
                                            </div>
                                        </div>
                                        
                                        <h2 style="margin: 0 0 16px 0; color: #1a1a2e; font-size: 24px; font-weight: 600; text-align: center;">
                                            Password Reset Successful! ✓
                                        </h2>
                                        
                                        <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6; text-align: center;">
                                            Your password has been successfully reset.
                                        </p>
                                        
                                        <div style="background: #f0fdf4; border-left: 4px solid #10b981; border-radius: 6px; padding: 16px; margin: 24px 0;">
                                            <p style="margin: 0; color: #065f46; font-size: 14px; line-height: 1.6;">
                                                <strong>🔒 Security Notice:</strong> Your account is now secure with your new password. You can now log in using your updated credentials.
                                            </p>
                                        </div>
                                        
                                        <p style="margin: 20px 0; color: #4a5568; font-size: 14px; line-height: 1.6;">
                                            If you didn't make this change or believe an unauthorized person has accessed your account, please contact our support team immediately.
                                        </p>
                                        
                                        <div style="background: #fff7ed; border-left: 4px solid #f59e0b; border-radius: 6px; padding: 16px; margin: 24px 0;">
                                            <p style="margin: 0 0 10px 0; color: #92400e; font-size: 14px; line-height: 1.6;">
                                                <strong>⚠️ Security Tips:</strong>
                                            </p>
                                            <ul style="margin: 0; padding-left: 20px; color: #92400e; font-size: 13px; line-height: 1.6;">
                                                <li style="margin-bottom: 6px;">Never share your password with anyone</li>
                                                <li style="margin-bottom: 6px;">Use a unique password for DevConnect</li>
                                                <li style="margin-bottom: 0;">Enable two-factor authentication for extra security</li>
                                            </ul>
                                        </div>
                                        
                                        <p style="margin: 0; color: #4a5568; font-size: 14px; line-height: 1.6; text-align: center;">
                                            Thank you for keeping your account secure!
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%); padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                        <p style="margin: 0 0 10px 0; color: #718096; font-size: 14px;">
                                            Stay secure,<br>
                                            <strong style="color: #6B46C1;">The DevConnect Team</strong>
                                        </p>
                                        
                                        <p style="margin: 20px 0 0 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                            Need help? Contact us at <a href="mailto:support@devconnect.com" style="color: #7C3AED; text-decoration: none;">support@devconnect.com</a>
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                            
                            <!-- Disclaimer -->
                            <table role="presentation" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 20px auto 0;">
                                <tr>
                                    <td style="text-align: center; padding: 0 20px;">
                                        <p style="margin: 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                            This is a confirmation email that your DevConnect password was successfully changed.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """;

    sendHtmlEmail(toEmail, subject, body);
    }

    public void sendResendVerificationCodeEmail(String toEmail, String verificationCode, int expiryMinutes) {
    String subject = "DevConnect - New Verification Code";

    String body = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>New Verification Code</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); min-height: 100vh;">
                <table role="presentation" cellpadding="0" cellspacing="0" style="width: 100%; border-collapse: collapse;">
                    <tr>
                        <td style="padding: 40px 20px;">
                            <table role="presentation" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3); overflow: hidden;">
                                
                                <!-- Header with gradient -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #6B46C1 0%, #9333EA 50%, #7C3AED 100%); padding: 40px 30px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                            DevConnect
                                        </h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="margin: 0 0 16px 0; color: #1a1a2e; font-size: 24px; font-weight: 600;">
                                            New Verification Code
                                        </h2>
                                        
                                        <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                            Hello,
                                        </p>
                                        
                                        <p style="margin: 0 0 20px 0; color: #4a5568; font-size: 16px; line-height: 1.6;">
                                            You requested a new verification code. Here's your fresh code to complete the verification process:
                                        </p>
                                        
                                        <!-- Verification Code Box -->
                                        <div style="margin: 30px 0;">
                                            <div style="background: linear-gradient(135deg, #6B46C1 0%, #9333EA 50%, #7C3AED 100%); border-radius: 12px; padding: 3px;">
                                                <div style="background: #ffffff; border-radius: 10px; padding: 24px; text-align: center;">
                                                    <p style="margin: 0 0 8px 0; color: #718096; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;">
                                                        Your New Verification Code
                                                    </p>
                                                    <div style="font-family: 'Courier New', monospace; font-size: 36px; font-weight: 700; color: #6B46C1; letter-spacing: 8px; margin: 0;">
                                                        {{VERIFICATION_CODE}}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <div style="background: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 6px; padding: 16px; margin: 20px 0;">
                                            <p style="margin: 0; color: #92400e; font-size: 14px; line-height: 1.6;">
                                                <strong>⏱️ Note:</strong> This new code will expire in <strong>{{EXPIRY_TIME}} minutes</strong>. Your previous code has been invalidated.
                                            </p>
                                        </div>
                                        
                                        <p style="margin: 20px 0 10px 0; color: #4a5568; font-size: 14px; line-height: 1.6;">
                                            <strong>Having trouble receiving codes?</strong>
                                        </p>
                                        
                                        <ul style="margin: 0 0 20px 0; padding-left: 20px; color: #4a5568; font-size: 14px; line-height: 1.6;">
                                            <li style="margin-bottom: 6px;">Check your spam or junk folder</li>
                                            <li style="margin-bottom: 6px;">Make sure you're entering the most recent code</li>
                                            <li style="margin-bottom: 0;">Wait a few minutes before requesting another code</li>
                                        </ul>
                                        
                                        <p style="margin: 0; color: #4a5568; font-size: 14px; line-height: 1.6;">
                                            If you didn't request this code, you can safely ignore this email.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%); padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                                        <p style="margin: 0 0 10px 0; color: #718096; font-size: 14px;">
                                            Best regards,<br>
                                            <strong style="color: #6B46C1;">The DevConnect Team</strong>
                                        </p>
                                        
                                        <p style="margin: 20px 0 0 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                            Still having issues? Contact us at <a href="mailto:support@devconnect.com" style="color: #7C3AED; text-decoration: none;">support@devconnect.com</a>
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                            
                            <!-- Disclaimer -->
                            <table role="presentation" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: 20px auto 0;">
                                <tr>
                                    <td style="text-align: center; padding: 0 20px;">
                                        <p style="margin: 0; color: #a0aec0; font-size: 12px; line-height: 1.5;">
                                            This email was sent because you requested a new verification code for your DevConnect account.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.replace("{{VERIFICATION_CODE}}", verificationCode)
               .replace("{{EXPIRY_TIME}}", String.valueOf(expiryMinutes));

    sendHtmlEmail(toEmail, subject, body);
    }
}
