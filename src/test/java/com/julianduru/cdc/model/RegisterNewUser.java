package com.julianduru.cdc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * created by Julian Duru on 27/02/2023
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterNewUser {


    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;


}


