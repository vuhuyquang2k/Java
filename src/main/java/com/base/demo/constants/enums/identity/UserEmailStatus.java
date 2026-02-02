package com.base.demo.constants.enums.identity;

import com.base.demo.constants.enums.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserEmailStatus implements PersistableEnum<Integer> {
    VERIFIED(1, "Đã xác thực"),
    UNVERIFIED(0, "Chưa xác thực");

    private final Integer value;
    private final String name;

    public static UserEmailStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserEmailStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static UserEmailStatus fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (UserEmailStatus status : values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class UserEmailStatusConverter implements AttributeConverter<UserEmailStatus, Integer> {
        @Override
        public Integer convertToDatabaseColumn(UserEmailStatus status) {
            return status != null ? status.getValue() : null;
        }

        @Override
        public UserEmailStatus convertToEntityAttribute(Integer value) {
            return UserEmailStatus.fromValue(value);
        }
    }
}
