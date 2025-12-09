package dev.matejeliash.springbootbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;



// TODO comment and check all
    @RestController
    public class MessageController {

        @Autowired
        private MessageSource messageSource;

        // return object with all messages for specified locale from resources/messages
        @GetMapping("/messages")
        public ResponseEntity<Map<String, String>> getMessages(Locale locale) {

            // resources location , if not working use "messages/messages"
            ResourceBundle bundle = ResourceBundle.getBundle("messages.messages", locale);

            // all messages -> map -> JSON (in JS)
            Map<String, String> messages = new HashMap<>();
            for (String key : bundle.keySet()) {
                messages.put(key, messageSource.getMessage(key, null, locale));
            }

            return ResponseEntity.ok(messages);
        }
    }
