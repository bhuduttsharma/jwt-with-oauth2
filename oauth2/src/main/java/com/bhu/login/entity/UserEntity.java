package com.bhu.login.entity;


import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bhudutt
 */
@Entity
@Table(name = "USER_INFO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(name = "USER_NAME")
    private String userName;
    
    @Column(nullable = false, name = "EMAIL_ID", unique = true)
    private String emailId;
    
    @Column(name = "MOBILE_NUMBER")
    private String mobileNumber;
    
    @Column(nullable = false, name = "ROLES")
    private String roles;

    @Column(nullable = false, name = "PASSWORD")
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RefreshTokenEntity> refreshTokens;
}