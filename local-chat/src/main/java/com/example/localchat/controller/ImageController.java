package com.example.localchat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.localchat.service.ComfyUIService;

@RestController
public class ImageController {

    @Autowired
    private ComfyUIService comfyUIService;

    @GetMapping("/api/draw")
    public String draw(@RequestParam String prompt) {
        // return image path or log
        return comfyUIService.generateImage(prompt);
    }
}