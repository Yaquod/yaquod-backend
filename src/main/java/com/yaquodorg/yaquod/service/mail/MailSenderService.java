package com.yaquodorg.yaquod.service.mail;

public interface MailSenderService {
  void sendEmail(String toEmail, String subject, String body);
}
