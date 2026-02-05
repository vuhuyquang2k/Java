package com.base.demo.constants.enums.deposit_request;

import com.base.demo.constants.enums.PersistableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DepositRequestStatus implements PersistableEnum<Integer> {
    PENDING(1, "Chờ duyệt"),
    APPROVED(2, "Đã duyệt"),
    REJECTED(3, "Từ chối"),
    CANCELLED(4, "Đã huỷ");

    private final Integer value;
    private final String name;

    public static DepositRequestStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (DepositRequestStatus status : DepositRequestStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }

        return null;
    }

    public static DepositRequestStatus fromName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (DepositRequestStatus status : DepositRequestStatus.values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }

        return null;
    }

    @Converter(autoApply = true)
    public static class DepositRequestStatusConverter implements AttributeConverter<DepositRequestStatus, Integer> {
        @Override
        public Integer convertToDatabaseColumn(DepositRequestStatus status) {
            return status != null ? status.getValue() : null;
        }

        @Override
        public DepositRequestStatus convertToEntityAttribute(Integer value) {
            return DepositRequestStatus.fromValue(value);
        }
    }
}
