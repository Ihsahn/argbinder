package io.ihsahn.argbinder;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ArgBinder {

    private final Object target;
    private final Resolver resolver  = new DefaultResolver();

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
            ValueWrapper wrappedValue = handleConversion(propertyDescriptor, paramName, value);
            ensureCollectionInitialization(paramName, propertyDescriptor, wrappedValue);
            PropertyUtils.setNestedProperty(target, paramName, wrappedValue.value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"raw"})
    private void ensureCollectionInitialization(String paramName, PropertyDescriptor propertyDescriptor, ValueWrapper wrappedValue) throws IllegalAccessException, InvocationTargetException {
        if (!wrappedValue.indexed) {
            return;
        }
        if (List.class.equals(wrappedValue.kind)) {
            List list;
            if (propertyDescriptor.getReadMethod().invoke(target)==null) {
                list = new ArrayList();
                propertyDescriptor.getWriteMethod().invoke(target, list);
            } else {
                list = ((List)propertyDescriptor.getReadMethod().invoke(target));
            }
            int size = list.size();
            int index = resolver.getIndex(paramName);
            if (index>=size) {
                list.add(null);
            }
        }
    }

    @SuppressWarnings({"raw"})
    private ValueWrapper handleConversion(PropertyDescriptor propertyDescriptor, String paramName, Object value) {
        Method readMethod = propertyDescriptor.getReadMethod();
        Type genericReturnType = readMethod.getGenericReturnType();
        boolean indexed = resolver.isIndexed(paramName);
        ValueWrapper vw = new ValueWrapper();
        vw.indexed = indexed;
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            Type rawType = parameterizedType.getRawType();
            Class<? extends Type> rawTypeClass = rawType.getClass();
            if (rawTypeClass.isInstance(List.class)) {
                vw.kind = List.class;
                if (indexed) {
                   //it it's indexed we'll need single value
                    if (isEnum(parameterizedType)) {
                        value = convertToEnumValue(value, parameterizedType);
                    }
                } else {
                    value = convertToList(value.toString(), parameterizedType, paramName);
                }
            } else {
                throw new IllegalArgumentException("Unsupported bind type: "+rawTypeClass.getName());
            }
        } else if (isEnum(genericReturnType)) {
            value = convertToEnumValue(value, genericReturnType);
        }
        vw.value = value;
        return vw;
    }

    private List<?> convertToList(Object value, ParameterizedType parameterizedType, String paramName) {
        List<String> values = Arrays.asList(value.toString().split(","));
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length > 1) {
            throw new IllegalArgumentException("Invalid argument type for " + paramName);
        }
        //optional conversion
        if (isEnum(actualTypeArguments[0])) {
            return values.stream().map(s -> convertToEnumValue(s, actualTypeArguments[0])).collect(Collectors.toList());
        }
        return values;
    }

    @SuppressWarnings({"unchecked", "raw"})
    private Enum<?> convertToEnumValue(Object value, Type genericReturnType) {
        return Enum.valueOf((Class<Enum>) genericReturnType, value.toString());
    }

    private boolean isEnum(Type type) {
        return type instanceof Class<?> && ((Class<?>) type).isEnum();
    }

    //tiny wrapper, so we won't have discover these things repeatedly in each method
    static class ValueWrapper {
        Class<?> kind;
        Object value;
        boolean indexed;
    }
}
