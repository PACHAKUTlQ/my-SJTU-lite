package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileEntityDto {
    private String account;
    private String cardNo;
    private String cardType;
    private String classNo;
    private String code;
    private String email;
    private String gender;
    private String id;
    private String kind;
    private String mobile;
    private String name;
    private Long timeZone;
    private String unionID;
    private String userType;
    private ProfileIdentity[] identities;

    @JsonProperty("account")
    public String getAccount() { return account; }
    @JsonProperty("account")
    public void setAccount(String value) { this.account = value; }

    @JsonProperty("cardNo")
    public String getCardNo() { return cardNo; }
    @JsonProperty("cardNo")
    public void setCardNo(String value) { this.cardNo = value; }

    @JsonProperty("cardType")
    public String getCardType() { return cardType; }
    @JsonProperty("cardType")
    public void setCardType(String value) { this.cardType = value; }

    @JsonProperty("classNo")
    public String getClassNo() { return classNo; }
    @JsonProperty("classNo")
    public void setClassNo(String value) { this.classNo = value; }

    @JsonProperty("code")
    public String getCode() { return code; }
    @JsonProperty("code")
    public void setCode(String value) { this.code = value; }

    @JsonProperty("email")
    public String getEmail() { return email; }
    @JsonProperty("email")
    public void setEmail(String value) { this.email = value; }

    @JsonProperty("gender")
    public String getGender() { return gender; }
    @JsonProperty("gender")
    public void setGender(String value) { this.gender = value; }

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("kind")
    public String getKind() { return kind; }
    @JsonProperty("kind")
    public void setKind(String value) { this.kind = value; }

    @JsonProperty("mobile")
    public String getMobile() { return mobile; }
    @JsonProperty("mobile")
    public void setMobile(String value) { this.mobile = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("timeZone")
    public Long getTimeZone() { return timeZone; }
    @JsonProperty("timeZone")
    public void setTimeZone(Long value) { this.timeZone = value; }

    @JsonProperty("unionId")
    public String getUnionID() { return unionID; }
    @JsonProperty("unionId")
    public void setUnionID(String value) { this.unionID = value; }

    @JsonProperty("userType")
    public String getUserType() { return userType; }
    @JsonProperty("userType")
    public void setUserType(String value) { this.userType = value; }

    @JsonProperty("identities")
    public ProfileIdentity[] getIdentities() { return identities; }
    @JsonProperty("identities")
    public void setIdentities(ProfileIdentity[] value) { this.identities = value; }
}
