package com.project.controller;

import com.project.UserServiceVerify;
import com.project.Utility;
import com.project.dao.UserRepository;
import com.project.entities.User;
import com.project.helper.Message;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    @Autowired
  private   UserServiceVerify service;
    @GetMapping("/")
    public String home(){

        return "home";
    }
    @GetMapping("/about")
    public String about(Model model){
  model.addAttribute("title","This is title");
        return "about";
    }
    @GetMapping("/signUp")
    public String signUp(Model model){
      model.addAttribute("user" ,new User());
        return "signUp";
    }
    //this user for register user
    @PostMapping("/do_register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value = "agreement",defaultValue = "false") boolean agreement, Model model, HttpSession session, HttpServletRequest request, RedirectAttributes attributes){
        try {
            if (result1.hasErrors()){

                model.addAttribute("user",user);
                return "signUp";
            }
            if (!agreement){
                System.out.println("you have not agreed the terms and condition");
                throw new Exception("you have not agreed the terms and condition");
            }
          user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            user.setEnabled(false);
            user.setImageUrl("default.png");
            System.out.println("agreement"+agreement);
            String randomCode = java.util.UUID.randomUUID().toString();
            user.setVerificationCode(randomCode);
            System.out.println("User"+user);
            User result=  this.userRepository.save(user);
            String siteURL= Utility.getSiteURL(request);
            service.sendVerificationEmail( user,  siteURL);
            model.addAttribute("user" ,new User());

            session.setAttribute("message",new Message("Successfully Register","alert-success"));
        }
        catch (Exception e){
            e.printStackTrace();
            model.addAttribute("user",user);
            session.setAttribute("message",new Message("Something went wrong !!"+e.getMessage(),"alert-danger"));
        }
        return "register_success";
    }
    @GetMapping("/customLogin")
    public String customLogin(){
        return "login";
    }
}
