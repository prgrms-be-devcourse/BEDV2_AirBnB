package com.prgrms.airbnb.common.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Phone {
    private final String regx = "^\\d{3}-\\d{3,4}-\\d{4}$";
    private String number;

    public Phone(String number) {
        validationPhone(number);
        this.number = number;
    }

    public static Phone of(String number) {
        return new Phone(number);
    }

    public String getNumber() {
        return number;
    }

    public void validationPhone(String number) {
        if (StringUtils.isBlank(number) || !number.matches(regx)) {
            throw new IllegalArgumentException();
        }
    }

}
