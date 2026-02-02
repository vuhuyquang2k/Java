package com.base.demo.constants.enums.wallet_transaction;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WalletDirection {
    CREDIT(1, "Vào ví"),
    DEBIT(2, "Ra khỏi ví");

    private final Integer value;
    private final String name;

    public static WalletDirection fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (WalletDirection status : WalletDirection.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static WalletDirection fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (WalletDirection status : WalletDirection.values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class WalletDirectionConverter implements AttributeConverter<WalletDirection, Integer> {
        @Override
        public Integer convertToDatabaseColumn(WalletDirection direction) {
            return direction.getValue();
        }

        @Override
        public WalletDirection convertToEntityAttribute(Integer value) {
            return WalletDirection.fromValue(value);
        }
    }
}
