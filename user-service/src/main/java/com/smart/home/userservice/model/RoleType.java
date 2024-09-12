package com.smart.home.userservice.model;

import lombok.Getter;

@Getter
public enum RoleType {

    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private String value;

    RoleType(String value) {
        this.value = value;
    }

}
