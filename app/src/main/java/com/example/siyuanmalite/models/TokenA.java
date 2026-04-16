package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenA {
    private String accessToken;
    private String expiresIn;
    private String idToken;
    private String refreshToken;
    private String tokenType;

    @JsonProperty("access_token")
    public String getAccessToken() { return accessToken; }
    @JsonProperty("access_token")
    public void setAccessToken(String value) { this.accessToken = value; }

    @JsonProperty("expires_in")
    public String getExpiresIn() { return expiresIn; }
    @JsonProperty("expires_in")
    public void setExpiresIn(String value) { this.expiresIn = value; }

    @JsonProperty("id_token")
    public String getIDToken() { return idToken; }
    @JsonProperty("id_token")
    public void setIDToken(String value) { this.idToken = value; }

    @JsonProperty("refresh_token")
    public String getRefreshToken() { return refreshToken; }
    @JsonProperty("refresh_token")
    public void setRefreshToken(String value) { this.refreshToken = value; }

    @JsonProperty("token_type")
    public String getTokenType() { return tokenType; }
    @JsonProperty("token_type")
    public void setTokenType(String value) { this.tokenType = value; }
}
