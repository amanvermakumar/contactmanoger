package com.project;

import com.project.dao.UserRepository;
import com.project.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Transactional
@Service
public class UserService {
    @Autowired
        private UserRepository customerRepo;
    private User user;

private Principal principal;


    public void updateResetPasswordToken(String token, String email) throws UsernameNotFoundException{
        User customer = this.customerRepo.getUserByUserName(email);


        if (customer != null) {
            customer.setResetPasswordToken(token);
            customerRepo.save(customer);
        } else {
            throw new UsernameNotFoundException("Could not find any customer with the email " + email);
        }
    }

    public User getByResetPasswordToken(String token) {
        return customerRepo.findByResetPasswordToken(token);
    }

    public void updatePassword(User customer, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        customer.setPassword(encodedPassword);

        customer.setResetPasswordToken(null);
        customerRepo.save(customer);
    }
}
