package com.base.demo.constants.enums.user;

import com.base.demo.constants.enums.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserProvider implements PersistableEnum<Integer> {
    GOOGLE(1, "GOOGLE");

    private final Integer value;
    private final String name;

    public static UserProvider fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserProvider identity : UserProvider.values()) {
            if (identity.getValue().equals(value)) {
                return identity;
            }
        }
        return null;
    }

    public static UserProvider fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (UserProvider identity : UserProvider.values()) {
            if (identity.getName().equalsIgnoreCase(name)) {
                return identity;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class UserIdentityConverter implements AttributeConverter<UserProvider, Integer> {
        @Override
        public Integer convertToDatabaseColumn(UserProvider identity) {
            return identity != null ? identity.getValue() : null;
        }

        @Override
        public UserProvider convertToEntityAttribute(Integer value) {
            return UserProvider.fromValue(value);
        }
    }
}
