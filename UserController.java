package com.project.controller;

import com.project.dao.ContactRepository;
import com.project.dao.UserRepository;
import com.project.entities.Contact;
import com.project.entities.User;
import com.project.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @ModelAttribute
    public void addCommonData(Model model ,Principal principal){
        String userName=principal.getName();
        System.out.println("USERNAME " +userName);
        User  user=userRepository.getUserByUserName(userName);
        System.out.println("User "+user);
        model.addAttribute("user",user);

    }
    //dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal){

        return "normal/user_dashboard";
    }
    //open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title","OpenContact");
        model.addAttribute("contact",new Contact());

        return "normal/add_contact_form";
    }
    //processing add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session){
        try {
            String name=principal.getName();
            //      System.out.println(name);
            User user=this.userRepository.getUserByUserName(name);

            //processing and uploaded

            if(file.isEmpty()){
                System.out.println("file is not empty");
                contact.setImage("contact.png");
            }
            else {
                // upload the file
                contact.setImage(file.getOriginalFilename());
               File file1 = new ClassPathResource("static/image").getFile();
              Path path =  Paths.get(file1.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("image is uploaded");
            }
            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            //   System.out.println("DATA "+contact);
            System.out.println("Added to data base");

            // message success
            session.setAttribute("message",new Message("your contact is added !! Add more..","success"));

        }catch (Exception e){
            System.out.println("Error "+e.getMessage());
            e.printStackTrace();
            session.setAttribute("message",new Message("Some went wrong !! try again","danger"));
        }
        return "normal/add_contact_form";
    }
//show contacts handler
    //per page=5[n]
    //current page=0 [page]
    @GetMapping("/show-contacts/{page}")
    public String showContact(@PathVariable("page") Integer page, Model model,Principal principal){


     String user=principal.getName();
      User user1=this.userRepository.getUserByUserName(user);
       Pageable pageable = PageRequest.of(page,3);
       Page<Contact> contacts=  this.contactRepository.findContactsByUser(user1.getId(),pageable);
       model.addAttribute("contacts",contacts);
       model.addAttribute("currentPage",page);
       model.addAttribute("totalPages",contacts.getTotalPages());

        //othermethof
        return "normal/show_contacts";
    }
    @GetMapping("/contact/{cId}")
    public String showContentDetail(@PathVariable("cId") Integer cId,Model model,Principal principal){
        Optional<Contact> optional =  this.contactRepository.findById(cId);
        Contact contact=optional.get();
        String userName=principal.getName();
        User user=this.userRepository.getUserByUserName(userName);
        if(user.getId()==contact.getUser().getId())
        model.addAttribute("contact" ,contact);
        return "normal/contact_detail";
    }
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId,HttpSession session,Principal principal){
        Optional<Contact> optional =  this.contactRepository.findById(cId);
        Contact contact=optional.get();
        contact.setUser(null);
         User user=this.userRepository.getUserByUserName(principal.getName());
         user.getContacts().remove(contact);
         this.userRepository.save(user);
        session.setAttribute("message",new Message("Contact deleted successfully","success"));
        return "redirect:/user/show-contacts/0";
    }

    @PostMapping("/update-contact/{cid}")
    //open update form handler
    public String updateForm(@PathVariable("cid") Integer cId,Model model){
       Contact contact= this.contactRepository.findById(cId).get();
       model.addAttribute("contact",contact);
        return "normal/update_form";
    }

    //update handler
    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal){
          try {

            Contact contact1=  this.contactRepository.findById(contact.getcId()).get();
              if(!file.isEmpty()){
                  //delete old photo
                  File deletefile=new ClassPathResource("static/image").getFile();
               File file2=new File(deletefile,contact1.getImage());
                file2.delete();


                  //new update photo
                  File file1=new ClassPathResource("static/image").getFile();
                  Path path=Paths.get(file1.getAbsolutePath()+File.separator+file.getOriginalFilename());
                  Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
                  contact.setImage(file.getOriginalFilename());

              }
              else
              {
                  contact.setImage(contact1.getImage());
              }
          }
          catch (Exception e){
              e.printStackTrace();
          }
          String user=principal.getName();
          User user1=this.userRepository.getUserByUserName(user);
          contact.setUser(user1);

          this.contactRepository.save(contact);
        System.out.println("CONTACT NAME "+contact.getName());
        System.out.println("CONTACT Id "+contact.getcId());
        session.setAttribute("message",new Message("Contact update successfully","success"));

        return "redirect:/user/contact/"+contact.getcId();

    }
    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title","Profile Page");

     return "normal/profile";
    }

    @GetMapping("/settings")
    public String settings(){

        return "normal/settings";
    }
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword")String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, RedirectAttributes redirectAttributes){
        System.out.println(oldPassword);
        System.out.println(newPassword);
        String username=principal.getName();
        User user=this.userRepository.getUserByUserName(username);
        String password=user.getPassword();
        if (this.bCryptPasswordEncoder.matches(oldPassword,password)){
            user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "password change Successfully !....!!...");
        }
        else {
            redirectAttributes.addFlashAttribute("success", " please enter correct old password !....!!...");
        }

        return "redirect:/user/settings";

    }
}
