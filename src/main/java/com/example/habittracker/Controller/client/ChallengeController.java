package com.example.habittracker.Controller.client;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.*;
import com.example.habittracker.Domain.Challenge;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Service.ChallengeService;
import com.example.habittracker.Service.GeminiService;
import com.example.habittracker.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;
    private final JwtUtil jwtUtil;
    private final TokenUtil tokenUtil;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final GeminiService geminiService;

    public ChallengeController(ChallengeService challengeService, JwtUtil jwtUtil, TokenUtil tokenUtil, UserService userService, ObjectMapper objectMapper, GeminiService geminiService) {
        this.challengeService = challengeService;
        this.jwtUtil = jwtUtil;
        this.tokenUtil = tokenUtil;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.geminiService = geminiService;
    }

    @PostMapping("/save")
    public String createChallenge(HttpServletRequest request,@RequestParam("title") String title,
                                  @RequestParam("description") String description,
                                  @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                  @RequestParam("day") Long day,
                                  @RequestParam("habits") String habitsJson,
                                  @RequestParam("dailies") String dailiesJson, RedirectAttributes redirectAttributes) throws Exception{
        try{
            List<HabitDTO> habits = objectMapper.readValue(habitsJson, new TypeReference<List<HabitDTO>>() {});
            List<DailyDTO> dailies = objectMapper.readValue(dailiesJson, new TypeReference<List<DailyDTO>>() {});

            ChallengeDTO challengeDTO = ChallengeDTO.builder()
                    .title(title)
                    .description(description)
                    .endDate(endDate)
                    .day(day)
                    .habits(habits)
                    .dailies(dailies)
                    .build();

            User user = getUserFromRequest(request);
            this.challengeService.createChallenge(challengeDTO, user);
            redirectAttributes.addFlashAttribute("success", "Tạo Thử Thách Thành Công!");
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/challenge_overview";
        }
        return "redirect:/challenge_overview";
    }

    @GetMapping("/{challengeId}")
    @ResponseBody
    public ResponseEntity<ChallengeDTO> getChallenge(HttpServletRequest request,@PathVariable Long challengeId,@RequestParam(value = "creator",defaultValue = "true") Boolean creator) {
        User user;
        //creator để lấy ra thông tin userChallenge của người tạo challenge community
        if(creator){
            user = getUserFromRequest(request);
        }else{
            Challenge challenge = challengeService.getChallengeById(challengeId);
            user = this.userService.getUserById(challenge.getCreatorId());
        }
        ChallengeDTO challengeDTO = challengeService.getChallengeDTOById(challengeId, user);
        if (challengeDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(challengeDTO);
    }

    @PostMapping("/{challengeId}")
    public String updateChallenge(HttpServletRequest request,
                                  @RequestParam("challengeId") Long challengeId,
                                  @RequestParam("title") String title,
                                  @RequestParam("description") String description,
                                  @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                  @RequestParam("day") Long day,
                                  @RequestParam("habits") String habitsJson,
                                  @RequestParam("dailies") String dailiesJson, RedirectAttributes redirectAttributes) throws Exception{
        try{
            List<HabitDTO> habits = objectMapper.readValue(habitsJson, new TypeReference<List<HabitDTO>>() {});
            List<DailyDTO> dailies = objectMapper.readValue(dailiesJson, new TypeReference<List<DailyDTO>>() {});

            ChallengeDTO challengeDTO = ChallengeDTO.builder()
                    .challengeId(challengeId)
                    .title(title)
                    .description(description)
                    .endDate(endDate)
                    .day(day)
                    .habits(habits)
                    .dailies(dailies)
                    .build();

            User user = getUserFromRequest(request);
            this.challengeService.updateChallenge(challengeDTO, user);
            redirectAttributes.addFlashAttribute("success", "Chỉnh sửa Thử Thách Thành Công!");
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/challenge_overview";
        }
        return "redirect:/challenge_overview";
    }

    @GetMapping("/delete/{challengeId}")
    public String deleteChallenge(HttpServletRequest request,@PathVariable Long challengeId, RedirectAttributes redirectAttributes) {
        try{
            User user = getUserFromRequest(request);
            this.challengeService.deleteChallenge(challengeId,user);
            redirectAttributes.addFlashAttribute("success", "Xóa thử thách thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/challenge_overview";
        }
        return "redirect:/challenge_overview";
    }

    @GetMapping("/detail/challenge/{id}")
    @ResponseBody
    public ResponseEntity<ChallengeDTO> getChallengeDetails(HttpServletRequest request, @PathVariable Long id, @RequestParam(value = "creator",defaultValue = "true") Boolean creator) {
        User user;
        //creator = false khi người dùng ở cộng đồng xem chi tiết quá trình của người dùng
        if(creator){
            user = getUserFromRequest(request);
        }else{
            Challenge challenge = challengeService.getChallengeById(id);
            user = this.userService.getUserById(challenge.getCreatorId());
        }

        ChallengeDTO challengeDTO = this.challengeService.getUserChallengeDetail(user,id);
        return ResponseEntity.ok().body(challengeDTO);
    }

    @PostMapping("/get_suggest")
    @ResponseBody
    public ResponseEntity<ChallengeDTO> suggestTask(@RequestBody SuggestRequest request) throws JsonProcessingException {
        String jsonResponse = this.geminiService.suggestHabits(request);
        ChallengeSuggestionWrapper wrapper = objectMapper.readValue(jsonResponse, ChallengeSuggestionWrapper.class);
        ChallengeDTO challengeDTO = wrapper.getChallengeDTO();
        return ResponseEntity.ok().body(challengeDTO);
    }

    private User getUserFromRequest(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email =  this.jwtUtil.getEmailFromToken(token);
        return this.userService.getUser(email);
    }
}
