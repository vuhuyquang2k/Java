package com.base.demo.constants.enums.wallet_transaction;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WalletTransactionDirection {
    CREDIT(1, "Vào ví"),
    DEBIT(2, "Ra khỏi ví");

    private final Integer value;
    private final String name;

    public static WalletTransactionDirection fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (WalletTransactionDirection status : WalletTransactionDirection.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public static WalletTransactionDirection fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (WalletTransactionDirection status : WalletTransactionDirection.values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class WalletDirectionConverter implements AttributeConverter<WalletTransactionDirection, Integer> {
        @Override
        public Integer convertToDatabaseColumn(WalletTransactionDirection direction) {
            return direction.getValue();
        }

        @Override
        public WalletTransactionDirection convertToEntityAttribute(Integer value) {
            return WalletTransactionDirection.fromValue(value);
        }
    }
}
