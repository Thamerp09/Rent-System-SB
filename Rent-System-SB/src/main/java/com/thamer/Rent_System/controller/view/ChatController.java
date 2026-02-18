package com.thamer.Rent_System.controller.view;

import com.thamer.Rent_System.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String userQuery = payload.get("query");
        
        System.out.println("User Question: " + userQuery); 
        
        // السطر المفقود الذي سبب المشكلة 👇
        String aiResponse = chatService.processUserQuestion(userQuery);

        Map<String, String> response = new HashMap<>();
        response.put("response", aiResponse);
        return response;
    }
}