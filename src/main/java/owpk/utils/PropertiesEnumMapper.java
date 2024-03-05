package owpk.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PropertiesEnumMapper {

    public static <T extends Enum<T>> Map<String, T> valuesMap(T[] values) {
        return Arrays.stream(values)
                .collect(Collectors.toMap(Enum::name, Function.identity()));
    };
}
