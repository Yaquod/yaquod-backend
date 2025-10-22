package com.yaquodorg.yaquod.entity;

public enum Role {
    CLIENT,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
