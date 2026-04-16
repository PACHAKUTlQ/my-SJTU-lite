package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * unicode-pay
 */
public class UnicodePay {
    private UnicodePayEntity[] entities;
    private long errno;
    private String error;
    private long total;

    @JsonProperty("entities")
    public UnicodePayEntity[] getEntities() { return entities; }
    @JsonProperty("entities")
    public void setEntities(UnicodePayEntity[] value) { this.entities = value; }

    @JsonProperty("errno")
    public long getErrno() { return errno; }
    @JsonProperty("errno")
    public void setErrno(long value) { this.errno = value; }

    @JsonProperty("error")
    public String getError() { return error; }
    @JsonProperty("error")
    public void setError(String value) { this.error = value; }

    @JsonProperty("total")
    public long getTotal() { return total; }
    @JsonProperty("total")
    public void setTotal(long value) { this.total = value; }
}
