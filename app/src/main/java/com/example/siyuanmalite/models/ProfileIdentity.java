package com.example.siyuanmalite.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileIdentity {
    private String admissionDate;
    private String classNo;
    private String code;
    private long createDate;
    private boolean defaultOptional;
    private String expireDate;
    private String gjm;
    private String graduateDate;
    private boolean isDefault;
    private String kind;
    private ProfileIdentityOrganize organize;
    private String photoURL;
    private String status;
    private String trainLevel;
    private long updateDate;
    private String userType;
    private String userTypeName;

    @JsonProperty("admissionDate")
    public String getAdmissionDate() { return admissionDate; }
    @JsonProperty("admissionDate")
    public void setAdmissionDate(String value) { this.admissionDate = value; }

    @JsonProperty("classNo")
    public String getClassNo() { return classNo; }
    @JsonProperty("classNo")
    public void setClassNo(String value) { this.classNo = value; }

    @JsonProperty("code")
    public String getCode() { return code; }
    @JsonProperty("code")
    public void setCode(String value) { this.code = value; }

    @JsonProperty("createDate")
    public long getCreateDate() { return createDate; }
    @JsonProperty("createDate")
    public void setCreateDate(long value) { this.createDate = value; }

    @JsonProperty("defaultOptional")
    public boolean getDefaultOptional() { return defaultOptional; }
    @JsonProperty("defaultOptional")
    public void setDefaultOptional(boolean value) { this.defaultOptional = value; }

    @JsonProperty("expireDate")
    public String getExpireDate() { return expireDate; }
    @JsonProperty("expireDate")
    public void setExpireDate(String value) { this.expireDate = value; }

    @JsonProperty("gjm")
    public String getGjm() { return gjm; }
    @JsonProperty("gjm")
    public void setGjm(String value) { this.gjm = value; }

    @JsonProperty("graduateDate")
    public String getGraduateDate() { return graduateDate; }
    @JsonProperty("graduateDate")
    public void setGraduateDate(String value) { this.graduateDate = value; }

    @JsonProperty("isDefault")
    public boolean getIsDefault() { return isDefault; }
    @JsonProperty("isDefault")
    public void setIsDefault(boolean value) { this.isDefault = value; }

    @JsonProperty("kind")
    public String getKind() { return kind; }
    @JsonProperty("kind")
    public void setKind(String value) { this.kind = value; }

    @JsonProperty("organize")
    public ProfileIdentityOrganize getOrganize() { return organize; }
    @JsonProperty("organize")
    public void setOrganize(ProfileIdentityOrganize value) { this.organize = value; }

    @JsonProperty("photoUrl")
    public String getPhotoURL() { return photoURL; }
    @JsonProperty("photoUrl")
    public void setPhotoURL(String value) { this.photoURL = value; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    @JsonProperty("status")
    public void setStatus(String value) { this.status = value; }

    @JsonProperty("trainLevel")
    public String getTrainLevel() { return trainLevel; }
    @JsonProperty("trainLevel")
    public void setTrainLevel(String value) { this.trainLevel = value; }

    @JsonProperty("updateDate")
    public long getUpdateDate() { return updateDate; }
    @JsonProperty("updateDate")
    public void setUpdateDate(long value) { this.updateDate = value; }

    @JsonProperty("userType")
    public String getUserType() { return userType; }
    @JsonProperty("userType")
    public void setUserType(String value) { this.userType = value; }

    @JsonProperty("userTypeName")
    public String getUserTypeName() { return userTypeName; }
    @JsonProperty("userTypeName")
    public void setUserTypeName(String value) { this.userTypeName = value; }
}