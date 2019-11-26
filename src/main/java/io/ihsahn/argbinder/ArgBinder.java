package io.ihsahn.argbinder;

import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArgBinder {

    private final Object target;

    public ArgBinder(Object target) {
        this.target = target;
    }

    public void parse(String[] args) {
        Iterator<String> argsIterator = Arrays.asList(args).iterator();
        while (argsIterator.hasNext()) {
            String paramName = argsIterator.next();
            Optional<String> value = Optional.empty();
            if (paramName.contains("=")) {
                String[] split = paramName.split("=", 2);
                paramName = split[0];
                value = Optional.of(split[1]);
            } else if (argsIterator.hasNext()) {
                value = Optional.of(argsIterator.next());
            }

            String finalParamName = paramName; //variables in lambdas have to be final...
            value.ifPresent(valueObject -> setProperty(finalParamName, valueObject));
        }
    }

    private void setProperty(String paramName, Object value) {
        try {
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(target, paramName);
            Method readMethod = propertyDescriptor.getReadMethod();
            value = handleConversion(readMethod, value);
            PropertyUtils.setNestedProperty(target, paramName, value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"raw"})
    private Object handleConversion(Method readMethod, Object value) {
        Type genericReturnType = readMethod.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            Type rawType = parameterizedType.getRawType();
            Class<? extends Type> rawTypeClass = rawType.getClass();
            if (rawTypeClass.isInstance(List.class)) {
                //prepare string-list of values
                List<String> values = Arrays.asList(value.toString().split(","));
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 1) {
                    throw new IllegalArgumentException("Invalid argument type for " + readMethod.getName());
                }
                //optional conversion
                if (isEnum(actualTypeArguments[0])) {
                    value = values.stream().map(s -> convertToEnumValue(s, actualTypeArguments[0])).collect(Collectors.toList());
                } else {
                    value = values;
                }
            }
        } else if (isEnum(genericReturnType)) {
            value = convertToEnumValue(value, genericReturnType);
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "raw"})
    private Enum<?> convertToEnumValue(Object value, Type genericReturnType) {
        return Enum.valueOf((Class<Enum>) genericReturnType, value.toString());
    }

    private boolean isEnum(Type type) {
        return type instanceof Class<?> && ((Class<?>) type).isEnum();
    }

}
