package com.project.dao;

import com.project.entities.Contact;
import com.project.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Integer> {


    //pegination
    @Query("from Contact as c where c.user.id=:userId")
    //pegable class ke pass currentPage-page and contact per page
    public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);


    public List<Contact> findByNameContainingAndUser(String keywords, User user);

}
