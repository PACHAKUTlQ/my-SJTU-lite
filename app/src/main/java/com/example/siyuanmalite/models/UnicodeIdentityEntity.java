package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnicodeIdentityEntity {
    private String action;
    private String actionColor;
    private String actionURL;
    private String backgroundColor;
    private String code;
    private String ec;
    private String icon;
    private String message;
    private String messageBackground;
    private String messageColor;
    private Boolean showIcon;
    private String slowIcon;
    private String slowMessage;
    private String slowMessageBackground;
    private String slowMessageColor;
    private Long status;

    @JsonProperty("action")
    public String getAction() { return action; }
    @JsonProperty("action")
    public void setAction(String value) { this.action = value; }

    @JsonProperty("actionColor")
    public String getActionColor() { return actionColor; }
    @JsonProperty("actionColor")
    public void setActionColor(String value) { this.actionColor = value; }

    @JsonProperty("actionUrl")
    public String getActionURL() { return actionURL; }
    @JsonProperty("actionUrl")
    public void setActionURL(String value) { this.actionURL = value; }

    @JsonProperty("backgroundColor")
    public String getBackgroundColor() { return backgroundColor; }
    @JsonProperty("backgroundColor")
    public void setBackgroundColor(String value) { this.backgroundColor = value; }

    @JsonProperty("code")
    public String getCode() { return code; }
    @JsonProperty("code")
    public void setCode(String value) { this.code = value; }

    @JsonProperty("ec")
    public String getEc() { return ec; }
    @JsonProperty("ec")
    public void setEc(String value) { this.ec = value; }

    @JsonProperty("icon")
    public String getIcon() { return icon; }
    @JsonProperty("icon")
    public void setIcon(String value) { this.icon = value; }

    @JsonProperty("message")
    public String getMessage() { return message; }
    @JsonProperty("message")
    public void setMessage(String value) { this.message = value; }

    @JsonProperty("messageBackground")
    public String getMessageBackground() { return messageBackground; }
    @JsonProperty("messageBackground")
    public void setMessageBackground(String value) { this.messageBackground = value; }

    @JsonProperty("messageColor")
    public String getMessageColor() { return messageColor; }
    @JsonProperty("messageColor")
    public void setMessageColor(String value) { this.messageColor = value; }

    @JsonProperty("showIcon")
    public Boolean getShowIcon() { return showIcon; }
    @JsonProperty("showIcon")
    public void setShowIcon(Boolean value) { this.showIcon = value; }

    @JsonProperty("slowIcon")
    public String getSlowIcon() { return slowIcon; }
    @JsonProperty("slowIcon")
    public void setSlowIcon(String value) { this.slowIcon = value; }

    @JsonProperty("slowMessage")
    public String getSlowMessage() { return slowMessage; }
    @JsonProperty("slowMessage")
    public void setSlowMessage(String value) { this.slowMessage = value; }

    @JsonProperty("slowMessageBackground")
    public String getSlowMessageBackground() { return slowMessageBackground; }
    @JsonProperty("slowMessageBackground")
    public void setSlowMessageBackground(String value) { this.slowMessageBackground = value; }

    @JsonProperty("slowMessageColor")
    public String getSlowMessageColor() { return slowMessageColor; }
    @JsonProperty("slowMessageColor")
    public void setSlowMessageColor(String value) { this.slowMessageColor = value; }

    @JsonProperty("status")
    public Long getStatus() { return status; }
    @JsonProperty("status")
    public void setStatus(Long value) { this.status = value; }
}
