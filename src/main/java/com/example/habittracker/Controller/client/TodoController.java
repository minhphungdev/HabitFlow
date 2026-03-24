package com.example.habittracker.Controller.client;

import com.example.habittracker.Auth.JwtUtil;
import com.example.habittracker.Auth.TokenUtil;
import com.example.habittracker.DTO.TodoDTO;
import com.example.habittracker.Domain.User;
import com.example.habittracker.Service.TodoService;
import com.example.habittracker.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.swing.text.html.parser.Entity;

@Controller
@RequestMapping("/todos")
public class TodoController {
    private final TokenUtil tokenUtil;
    private final JwtUtil jwtUtil;
    private final TodoService todoService;
    private final UserService userService;

    public TodoController(TokenUtil tokenUtil, JwtUtil jwtUtil, TodoService todoService, UserService userService) {
        this.tokenUtil = tokenUtil;
        this.jwtUtil = jwtUtil;
        this.todoService = todoService;
        this.userService = userService;
    }

    @PostMapping("/save")
    public String saveTodo(@ModelAttribute("newTodo") TodoDTO todoDTO, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try{
            User user = getUserFromRequest(request);
            this.todoService.saveTodo(todoDTO, user);
            redirectAttributes.addFlashAttribute("success", "Thêm mới thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", e.getMessage());
            return "redirect:/overview";
        }

        return "redirect:/overview";
    }

    @GetMapping("/{todoId}")
    @ResponseBody
    public ResponseEntity<TodoDTO> getUpdateTodo(HttpServletRequest request, @PathVariable("todoId")Long id){
        User user = getUserFromRequest(request);
        TodoDTO todoDTO = this.todoService.getUpdateTodo(user, id);

        return ResponseEntity.ok().body(todoDTO);
    }

    @PostMapping("/{todoId}")
    public String updateTodo(HttpServletRequest request,@PathVariable("todoId") Long todoId,@ModelAttribute("newTodo")TodoDTO todoDTO,RedirectAttributes redirectAttributes){
        try{
            User user = getUserFromRequest(request);

            this.todoService.updateTodo(todoDTO, user, todoId);
            redirectAttributes.addFlashAttribute("success","Sửa việc cần làm thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", e.getMessage());
            return "redirect:/overview";
        }
        return "redirect:/overview";
    }

    @GetMapping("/delete/{todoId}")
    public String deleteTodo(HttpServletRequest request, @PathVariable("todoId")Long todoId,RedirectAttributes redirectAttributes){
        try{
            User user = getUserFromRequest(request);

            this.todoService.deleteTodo(user,todoId);
            redirectAttributes.addFlashAttribute("success","Xóa việc cần làm thành công!");
        }catch(RuntimeException e){
            redirectAttributes.addFlashAttribute("fail", e.getMessage());
            return "redirect:/overview";
        }

        return "redirect:/overview";
    }

    @GetMapping("/{todoId}/completion")
    @ResponseBody
    public ResponseEntity<TodoDTO> updateTodoCompletion(HttpServletRequest request,@PathVariable Long todoId) {
        User user = getUserFromRequest(request);
        TodoDTO todoDTO = todoService.updateTodoCompletion(user,todoId,true);
        return ResponseEntity.ok().body(todoDTO);
    }

    @GetMapping("/{todoId}/subtasks/{subtaskId}/completion")
    @ResponseBody
    public ResponseEntity<TodoDTO> updateSubtaskCompletion(HttpServletRequest request,@PathVariable Long todoId, @PathVariable Long subtaskId) {
        User user = getUserFromRequest(request);
        TodoDTO todoDTO = todoService.updateSubtaskCompletion(user,todoId, subtaskId);
        return ResponseEntity.ok().body(todoDTO);
    }

    private User getUserFromRequest(HttpServletRequest request) {
        String token = tokenUtil.getTokenFromCookies(request);
        String email =  this.jwtUtil.getEmailFromToken(token);
        return this.userService.getUser(email);
    }
}
