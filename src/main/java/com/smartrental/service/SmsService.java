package com.smartrental.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    private boolean configured;

    @PostConstruct
    public void init() {
        if (accountSid == null || accountSid.isBlank()
                || authToken == null || authToken.isBlank()
                || fromNumber == null || fromNumber.isBlank()) {
            log.warn("Twilio not configured — SMS sending disabled");
            configured = false;
            return;
        }
        Twilio.init(accountSid, authToken);
        configured = true;
        log.info("Twilio initialized");
    }

    public void sendSms(String to, String body) {
        if (!configured) {
            log.warn("Twilio not configured — SMS to {} not sent", to);
            return;
        }
        Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromNumber),
                body
        ).create();
        log.info("SMS sent to {}, sid: {}", to, message.getSid());
    }
}
