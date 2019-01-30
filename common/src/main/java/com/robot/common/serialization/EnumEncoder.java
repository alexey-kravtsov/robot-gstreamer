package com.robot.common.serialization;

import java.util.Arrays;

public class EnumEncoder {

    public static <T extends Enum<T>> T getType(Class<T> enumType, byte index) {
        return Arrays.stream(enumType.getEnumConstants())
                .filter(type -> type.ordinal() == index)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such element"));
    }

    public static <T extends Enum<T>> byte getValue(T item) {
        return (byte)item.ordinal();
    }
}
