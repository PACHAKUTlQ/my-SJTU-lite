package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JacLoginSmsVerifyResDto {
    private String error;
    private long errno;
    private boolean success;
    private JacLoginSmsSendInfoDto[] entities;

    @JsonProperty("entities")
    public JacLoginSmsSendInfoDto[] getEntities() { return entities; }
    @JsonProperty("entities")
    public void setEntities(JacLoginSmsSendInfoDto[] value) { this.entities = value; }

    @JsonProperty("success")
    public boolean getSuccess() { return success; }
    @JsonProperty("success")
    public void setSuccess(boolean value) { this.success = value; }

    @JsonProperty("error")
    public String getError() { return error; }
    @JsonProperty("error")
    public void setError(String value) { this.error = value; }

    @JsonProperty("errno")
    public long getErrno() {
        return errno;
    }

    @JsonProperty("errno")
    public void setErrno(long errno) {
        this.errno = errno;
    }
}