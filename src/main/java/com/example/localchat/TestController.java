package com.example.localchat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	@GetMapping("/hello")
    public String sayHello() {
        return "Hello! Local Gemini Backend is ready! 🚀";
    }
}
