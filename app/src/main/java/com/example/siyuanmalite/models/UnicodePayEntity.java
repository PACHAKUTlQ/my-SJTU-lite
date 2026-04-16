package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UnicodePayEntity {
    private String code;
    private String message;
    private Boolean showIcon;
    private Long status;
    private String[] offlineCodes;
    private Long offlineCodesExp;

    @JsonProperty("code")
    public String getCode() { return code; }
    @JsonProperty("code")
    public void setCode(String value) { this.code = value; }

    @JsonProperty("message")
    public String getMessage() { return message; }
    @JsonProperty("message")
    public void setMessage(String value) { this.message = value; }

    @JsonProperty("showIcon")
    public Boolean getShowIcon() { return showIcon; }
    @JsonProperty("showIcon")
    public void setShowIcon(Boolean value) { this.showIcon = value; }

    @JsonProperty("status")
    public Long getStatus() { return status; }
    @JsonProperty("status")
    public void setStatus(Long value) { this.status = value; }

    @JsonProperty("offlineCodes")
    public String[] getOfflineCodes() { return offlineCodes; }
    @JsonProperty("offlineCodes")
    public void setOfflineCodes(String[] value) { this.offlineCodes = value; }

    @JsonProperty("offlineCodesExp")
    public Long getOfflineCodesExp() { return offlineCodesExp; }
    @JsonProperty("offlineCodesExp")
    public void setOfflineCodesExp(Long value) { this.offlineCodesExp = value; }

    public UnicodeOfflineEntity toOfflineEntity()
    {
        return new UnicodeOfflineEntity(this.getOfflineCodes(), this.getOfflineCodesExp());
    }
}