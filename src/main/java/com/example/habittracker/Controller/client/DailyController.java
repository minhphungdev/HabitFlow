package com.example.habittracker.Controller.client;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.DailyDTO;
import com.example.habittracker.Service.DailyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dailies")
public class DailyController {
    private final JwtUtil jwtUtil;
    private final TokenUtil tokenUtil;
    private final DailyService dailyService;

    public DailyController(JwtUtil jwtUtil, TokenUtil tokenUtil, DailyService dailyService) {
        this.jwtUtil = jwtUtil;
        this.tokenUtil = tokenUtil;
        this.dailyService = dailyService;
    }

    @PostMapping("/create")
    public String createDaily(HttpServletRequest request, @ModelAttribute("newDaily")DailyDTO dailyDTO, RedirectAttributes redirectAttributes) {
        String username = getUserNameFromRequest(request);
        try{
            this.dailyService.createDaily(dailyDTO, username);
            redirectAttributes.addFlashAttribute("success", "Tạo thói quen hằng ngày thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/overview";
        }
        return "redirect:/overview";
    }

    @GetMapping("update/{dailyId}")
    @ResponseBody
    public ResponseEntity<DailyDTO> updateDaily(HttpServletRequest request, @PathVariable Long dailyId) {
        String username = getUserNameFromRequest(request);
        DailyDTO dailyUpdate = this.dailyService.getUpdateDaily(dailyId, username);
        return ResponseEntity.ok().body(dailyUpdate);
    }

    @PostMapping("update/{dailyId}")
    public String updateDaily(HttpServletRequest request, @PathVariable Long dailyId, @ModelAttribute("newDaily")DailyDTO dailyDTO, RedirectAttributes redirectAttributes) {
        String username = getUserNameFromRequest(request);
        try{
            this.dailyService.updateDaily(dailyDTO, username);
            redirectAttributes.addFlashAttribute("success", "Chỉnh sửa thành công!");
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/overview";
        }
        return "redirect:/overview";
    }

    @GetMapping("delete/{dailyId}")
    public String deleteDaily(HttpServletRequest request, @PathVariable Long dailyId,RedirectAttributes redirectAttributes) {
        String username = getUserNameFromRequest(request);
        try{
            this.dailyService.deleteDaily(dailyId , username);
            redirectAttributes.addFlashAttribute("success", "Xoá thói quen hàng ngày thành công!");
        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("failed", e.getMessage());
            return "redirect:/overview";
        }
        return "redirect:/overview";
    }

    @GetMapping("{dailyId}/task_checked")
    @ResponseBody
    public ResponseEntity<DailyDTO> updateTaskChecked(HttpServletRequest request, @PathVariable Long dailyId, @RequestParam("status") String status) {
        String username = getUserNameFromRequest(request);
        DailyDTO dailyTaskUpdate = this.dailyService.dailyTaskUpdate(username, dailyId, status);
        return ResponseEntity.ok().body(dailyTaskUpdate);
    }

    private String getUserNameFromRequest(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        return jwtUtil.getEmailFromToken(token);
    }
}
