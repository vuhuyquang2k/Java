package com.base.demo.constants.enums.user;

import com.base.demo.constants.enums.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserStatus implements PersistableEnum<Integer> {
    ACTIVE(1, "Hoạt động"),
    SUSPENDED(0, "Bị khóa");

    private final Integer value;
    private final String name;

    public static UserStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static UserStatus fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class UserStatusConverter implements AttributeConverter<UserStatus, Integer> {
        @Override
        public Integer convertToDatabaseColumn(UserStatus status) {
            return status != null ? status.getValue() : null;
        }

        @Override
        public UserStatus convertToEntityAttribute(Integer value) {
            return UserStatus.fromValue(value);
        }
    }
}
