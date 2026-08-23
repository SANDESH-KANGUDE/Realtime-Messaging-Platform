package com.company.chatplatform.aiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = "com.company.chatplatform")
public class AiApplication {
    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(AiApplication.class, args);
    }

    private static void loadEnvFile() {
        File dir = new File(".").getAbsoluteFile();
        File envFile = null;
        while (dir != null) {
            File candidate = new File(dir, ".env");
            if (candidate.exists() && candidate.isFile()) {
                envFile = candidate;
                break;
            }
            dir = dir.getParentFile();
        }

        if (envFile != null) {
            System.out.println("Loading environment variables from: " + envFile.getAbsolutePath());
            try {
                List<String> lines = Files.readAllLines(envFile.toPath());
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        int eqIdx = trimmed.indexOf('=');
                        String key = trimmed.substring(0, eqIdx).trim();
                        String val = trimmed.substring(eqIdx + 1).trim();
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1);
                        }
                        System.setProperty(key, val);
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to read .env file: " + e.getMessage());
            }
        } else {
            System.out.println(".env file not found in directory tree. Falling back to system environment variables.");
        }
    }
}
