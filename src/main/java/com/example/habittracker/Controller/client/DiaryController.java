package com.example.habittracker.Controller.client;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.DiaryDTO;
import com.example.habittracker.DTO.TaskDTO;
import com.example.habittracker.Domain.Diary;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Service.DiaryService;
import com.example.habittracker.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/diaries")
public class DiaryController {
    private final TokenUtil tokenUtil;
    private final JwtUtil jwtUtil;
    private final DiaryService diaryService;
    private final UserService userService;

    public DiaryController(TokenUtil tokenUtil, JwtUtil jwtUtil, DiaryService diaryService, UserService userService) {
        this.tokenUtil = tokenUtil;
        this.jwtUtil = jwtUtil;
        this.diaryService = diaryService;
        this.userService = userService;
    }

    @GetMapping("/{diaryId}")
    @ResponseBody
    public DiaryDTO getDiary(@PathVariable Long diaryId) {
        return diaryService.getDiaryDTO(diaryId);
    }

    @PostMapping("/save")
    public String saveDiary(@ModelAttribute("newDiary") DiaryDTO diaryDTO, HttpServletRequest request, @RequestParam("image") MultipartFile image, RedirectAttributes redirectAttributes) {
        User user = getUserFromRequest(request);
        try{
            String createComplete = diaryService.saveDiary(diaryDTO, image, user);
            redirectAttributes.addFlashAttribute("success", "Tạo nhật ký thành công! "+createComplete+" xu");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("failed", "Tạo nhật ký thất bại!"+ e.getMessage());
            return "redirect:/overview";
        }
        return "redirect:/overview";
    }

    @PostMapping("/update")
    public String updateDiary(@ModelAttribute("newDiary") DiaryDTO diaryDTO, @RequestParam(value = "image", required = false) MultipartFile image, RedirectAttributes redirectAttributes) {
        try{
            diaryService.updateDiary(diaryDTO, image);
            redirectAttributes.addFlashAttribute("success", "Cập nhật nhật ký thành công!");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("failed", "Cập nhật nhật ký thất bại!");
            return "redirect:/overview";
        }
        return "redirect:/overview";
    }

    @GetMapping("/delete/{diaryId}")
    public String deleteDiary(@PathVariable Long diaryId, RedirectAttributes redirectAttributes) {
        try{
            diaryService.deleteDiary(diaryId);
            redirectAttributes.addFlashAttribute("success", "Xóa nhật ký thành công!");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("failed", "Xóa nhật ký thất bại!");
            return "redirect:/overview";
        }
        return "redirect:/overview";
    }

    @GetMapping("/completed-tasks")
    @ResponseBody
    public List<TaskDTO> getCompletedTasks(HttpServletRequest request) {
        User user = getUserFromRequest(request);
        return diaryService.getCompletedTasks(user);
    }

    @PostMapping("/{diaryId}/update-tasks")
    @ResponseBody
    public DiaryDTO updateDiaryTasks(@PathVariable Long diaryId, HttpServletRequest request) {
        User user = getUserFromRequest(request);
        return diaryService.updateDiaryTasks(diaryId, user);
    }



    @GetMapping("/inChallenge/{UserChallengeId}")
    @ResponseBody
    public ResponseEntity<List<DiaryDTO>> getDiaryInChallenge(@PathVariable("UserChallengeId")Long userChallengeId){
        List<DiaryDTO> listDiary = this.diaryService.getDiariesInChallenge(userChallengeId);
        return ResponseEntity.ok().body(listDiary);
    }

    private User getUserFromRequest(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email =  this.jwtUtil.getEmailFromToken(token);
        return this.userService.getUser(email);
    }
}