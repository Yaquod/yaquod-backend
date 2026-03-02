package com.yaquodorg.yaquod.entity;

public enum Role {
  CLIENT,
  ADMIN,
  VEHICLE;

  public String getAuthority() {
    return "ROLE_" + this.name();
  }
}
