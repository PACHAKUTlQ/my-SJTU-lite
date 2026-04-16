package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payload {
    private String sig;
    private long ts;

    @JsonProperty("sig")
    public String getSig() { return sig; }
    @JsonProperty("sig")
    public void setSig(String value) { this.sig = value; }

    @JsonProperty("ts")
    public long getTs() { return ts; }
    @JsonProperty("ts")
    public void setTs(long value) { this.ts = value; }
}