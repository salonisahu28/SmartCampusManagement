package com.smartcampus.service;

import com.smartcampus.entity.Complaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Notifications module: sends an email whenever a complaint's status changes.
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${notifications.email-enabled:false}") boolean emailEnabled) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
    }

    public void notifyStatusChange(Complaint complaint) {
        if (!emailEnabled) {
            logger.info("Email notification disabled; status updated for complaint {}", complaint.getComplaintId());
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(complaint.getUser().getEmail());
        msg.setSubject("Complaint " + complaint.getComplaintId() + " status updated");
        msg.setText("Your complaint \"" + complaint.getDescription() + "\" is now: " + complaint.getStatus());
        mailSender.send(msg);
    }
}
