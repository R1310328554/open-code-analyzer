package org.keycloak.ssf.subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 9493 phone_number 格式的主体标识符，以电话号码标识主体。
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493#name-phone-number-identifier-for</p>
 */
public class PhoneNumberSubjectId extends SubjectId {

    public static final String TYPE = "phone_number";

    /** 主体的电话号码。 */
    @JsonProperty("phone_number")
    protected String phoneNumber;

    public PhoneNumberSubjectId() {
        super(TYPE);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "PhoneNumberSubjectId{" +
               "phoneNumber='" + phoneNumber + '\'' +
               '}';
    }
}
