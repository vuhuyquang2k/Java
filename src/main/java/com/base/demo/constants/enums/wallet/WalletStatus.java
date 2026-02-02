package com.base.demo.constants.enums.wallet;

import com.base.demo.constants.enums.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WalletStatus implements PersistableEnum<Integer> {
    ACTIVE(1, "Hoạt động"),
    FROZEN(2, "Đóng băng"),
    CLOSE(3, "Đóng");

    private final Integer value;
    private final String name;

    public static WalletStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (WalletStatus status : WalletStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static WalletStatus fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (WalletStatus status : WalletStatus.values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class WalletStatusConverter implements AttributeConverter<WalletStatus, Integer> {
        @Override
        public Integer convertToDatabaseColumn(WalletStatus status) {
            return status.getValue();
        }

        @Override
        public WalletStatus convertToEntityAttribute(Integer value) {
            return WalletStatus.fromValue(value);
        }
    }
}
