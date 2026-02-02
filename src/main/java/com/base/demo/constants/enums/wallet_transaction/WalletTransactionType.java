package com.base.demo.constants.enums.wallet_transaction;

import com.base.demo.constants.enums.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WalletTransactionType implements PersistableEnum<Integer> {
    DEPOSIT(1, "Nạp tiền"),
    REFUND(2, "Hoàn tiền"),
    RELEASE(3, "Huỷ tạm giữ tiền"),
    WITHDRAWAL(4, "Rút tiền"),
    PUCHASE(5, "Mua"),
    HOLD(6, "Tạm giữ");

    private final Integer value;
    private final String name;

    public static WalletTransactionType fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (WalletTransactionType status : WalletTransactionType.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }

        return null;
    }

    public static WalletTransactionType fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (WalletTransactionType status : WalletTransactionType.values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    @Converter(autoApply = true)
    public static class WalletTransactionTypeConverter implements AttributeConverter<WalletTransactionType, Integer> {
        @Override
        public Integer convertToDatabaseColumn(WalletTransactionType walletTransactionType) {
            return walletTransactionType.getValue();
        }

        @Override
        public WalletTransactionType convertToEntityAttribute(Integer value) {
            return WalletTransactionType.fromValue(value);
        }
    }
}
