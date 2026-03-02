package com.yaquodorg.yaquod.service.google;

import com.yaquodorg.yaquod.dtos.GoogleLoginDto;
import java.io.IOException;
import java.security.GeneralSecurityException;

public interface GoogleTokenService {
  GoogleLoginDto verifyIdToken(String idTokenString) throws GeneralSecurityException, IOException;
}
