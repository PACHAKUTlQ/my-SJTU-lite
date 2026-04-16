package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnicodeOfflineEntity {
    public UnicodeOfflineEntity()
    {

    }
    public UnicodeOfflineEntity(String[] offlineCodes, Long offlineCodesExp)
    {
        this.offlineCodes = offlineCodes;
        this.offlineCodesExp = offlineCodesExp;
    }
    private String[] offlineCodes;
    private Long offlineCodesExp;

    @JsonProperty("offlineCodes")
    public String[] getOfflineCodes() { return offlineCodes; }
    @JsonProperty("offlineCodes")
    public void setOfflineCodes(String[] value) { this.offlineCodes = value; }

    @JsonProperty("offlineCodesExp")
    public Long getOfflineCodesExp() { return offlineCodesExp; }
    @JsonProperty("offlineCodesExp")
    public void setOfflineCodesExp(Long value) { this.offlineCodesExp = value; }

}
