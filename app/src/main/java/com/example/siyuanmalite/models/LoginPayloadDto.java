package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginPayloadDto {
    private long error;
    private Payload payload;
    private String type;

    @JsonProperty("error")
    public long getError() { return error; }
    @JsonProperty("error")
    public void setError(long value) { this.error = value; }

    @JsonProperty("payload")
    public Payload getPayload() { return payload; }
    @JsonProperty("payload")
    public void setPayload(Payload value) { this.payload = value; }

    @JsonProperty("type")
    public String getType() { return type; }
    @JsonProperty("type")
    public void setType(String value) { this.type = value; }
}