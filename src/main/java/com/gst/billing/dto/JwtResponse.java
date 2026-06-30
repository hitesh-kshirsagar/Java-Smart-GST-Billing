package com.gst.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Returned to the client after a successful login */
@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String username;
    private String role;
}
