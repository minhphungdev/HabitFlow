package com.example.habittracker.Service;

import com.example.habittracker.DTO.SuggestRequest;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {
    private final Client client;

    public GeminiService(@Value("${gemini.api.key}")String apiKey) {
        this.client = Client.builder().apiKey(apiKey).build();
    }
    public String suggestHabits(SuggestRequest request) {

        String prompt = String.format(
                "Bạn là chuyên gia phát triển bản thân và quản lý thói quen. " +
                        "Dựa trên thông tin thử thách sau, gợi ý %s thói quen (habits) và %s thói quen hàng ngày (dailies) phù hợp. " +
                        "Nếu thông tin thử thách không đủ, hãy tạo gợi ý chung và hữu ích.\n\n" +
                        "Thông tin thử thách:\n" +
                        "- Tiêu đề: \"%s\"\n" +
                        "- Mô tả: \"%s\"\n" +
                        "- Số ngày: \"%s\"\n\n" +
                        "Quy tắc gợi ý:\n" +
                        "• Với habits: cung cấp title, description, type (POSITIVE, NEGATIVE, hoặc BOTH), difficulty (EASY/MEDIUM/HARD), targetCount (số lần cần đạt; **phải từ 1 trở lên**).\n" +
                        "• Với dailies: cung cấp title, description, difficulty, repeatFrequency (DAILY/WEEKLY/MONTHLY), repeatEvery (ví dụ: 1), " +
                        "repeatDays (chỉ khi WEEKLY, ví dụ [\"MONDAY\",\"WEDNESDAY\"]), repeatMonthDays (chỉ khi MONTHLY, ví dụ [1,15]).\n\n" +
                        "**Chỉ trả về JSON object thuần túy, không có bất kỳ văn bản, ký tự thừa hay markdown (ví dụ: dấu ba backticks ```) nào khác. Bắt đầu trực tiếp với dấu ngoặc nhọn mở `{` của JSON object.**\n" +
                        "{\n" +
                        "  \"challengeDTO\": {\n" +
                        "    \"habits\": [\n" +
                        "      {\n" +
                        "        \"title\": \"Tiêu đề thói quen\",\n" +
                        "        \"description\": \"Mô tả chi tiết\",\n" +
                        "        \"type\": \"POSITIVE\"//hoặc \"BOTH\",\"NEGATIVE\",\n" +
                        "        \"difficulty\": \"EASY\"//hoặc \"MEDIUM\",\"HARD\",\n" +
                        "        \"targetCount\": 1 // Ví dụ: targetCount >= 1\n" +
                        "      }\n" +
                        "      /* … mục khác … */\n" +
                        "    ],\n" +
                        "    \"dailies\": [\n" +
                        "      {\n" +
                        "        \"title\": \"Tiêu đề thói quen hàng ngày\",\n" +
                        "        \"description\": \"Mô tả chi tiết\",\n" +
                        "        \"difficulty\": \"EASY\"//hoặc \"MEDIUM\",\"HARD\",\n" +
                        "        \"repeatFrequency\": \"DAILY\"//hoặc \"WEEKLY\",\"MONTHLY\",\n" +
                        "        \"repeatEvery\": 1,\n" +
                        "        \"repeatDays\": [],\n" +
                        "        \"repeatMonthDays\": []\n" +
                        "      }\n" +
                        "      /* … mục khác … */\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}\n",

                request.getHabitSuggestNum(),
                request.getDailySuggestNum(),
                request.getTitle(),
                request.getDescription(),
                request.getDurationDay()
        );

        GenerateContentResponse resp = client.models.generateContent("gemini-2.5-flash", prompt, null);
        return resp.text();
    }
}
