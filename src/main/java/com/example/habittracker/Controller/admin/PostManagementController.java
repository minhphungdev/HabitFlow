package com.example.habittracker.Controller.admin;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Domain.UserChallenge;
import com.example.habittracker.Service.ChallengeService;
import com.example.habittracker.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/posts")
public class PostManagementController {
    private final TokenUtil tokenUtil;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ChallengeService challengeService;

    public PostManagementController(TokenUtil tokenUtil, JwtUtil jwtUtil, UserService userService, ChallengeService challengeService) {
        this.tokenUtil = tokenUtil;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.challengeService = challengeService;
    }

    @GetMapping("")
    public String postManagement(HttpServletRequest request ,Model model) {
        User userAdmin = getUserAdmin(request);
        model.addAttribute("userAdmin", userAdmin);

        List<UserChallenge> challengeList = this.challengeService.getSharedChallenge();
        model.addAttribute("challengePost", challengeList);

        List<UserChallenge> challengePending = this.challengeService.getPendingChallenges();
        model.addAttribute("challengePending", challengePending);

        return "admin/postmanage";
    }

    @GetMapping("/{challengeId}")
    public String postManagement(@RequestParam(value = "approve")Boolean isApprove , @PathVariable("challengeId") Long challengeId, RedirectAttributes redirectAttributes) {
        try{
            this.challengeService.ChallengeApprove(challengeId, isApprove);
            redirectAttributes.addFlashAttribute("success", (isApprove?"Duyệt thử thách thành công!":"Từ chối thách thành công!"));
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", "Duyệt thử thách thất bại!"+e.getMessage());
            return "redirect:/admin/posts";
        }
        return "redirect:/admin/posts";
    }

    public User getUserAdmin(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email = jwtUtil.getEmailFromToken(token);

        return this.userService.getUser(email);
    }
}
