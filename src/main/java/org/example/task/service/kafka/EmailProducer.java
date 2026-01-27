package org.example.task.service.kafka;

import org.example.task.dto.EmailDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailProducer {

    private final KafkaTemplate<String, EmailDto> kafkaTemplate;

    public EmailProducer(KafkaTemplate<String, EmailDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${kafka.topic.emails}")
    private String emailTopic;

    public void sendEmail(EmailDto emailDto){
        kafkaTemplate.send(emailTopic, emailDto);
    }

}
