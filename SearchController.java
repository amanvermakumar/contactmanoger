package com.project.controller;

import com.project.dao.ContactRepository;
import com.project.dao.UserRepository;
import com.project.entities.Contact;
import com.project.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class SearchController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
@GetMapping("/search/{query}")
    public ResponseEntity<?> search(Principal principal, @PathVariable("query") String query){

    User user=this.userRepository.getUserByUserName(principal.getName());
    List<Contact> contacts=this.contactRepository.findByNameContainingAndUser(query,user);

    return ResponseEntity.ok(contacts);
}
}
