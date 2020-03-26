package io.ihsahn.argbinder;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class ArgBinder {

    private final Object target;
    private final Resolver resolver = new DefaultResolver();

    public ArgBinder(Object target) {
        this.target = target;
    }

    public void parse(String[] args) {
        Iterator<String> argsIterator = Arrays.asList(args).iterator();
        while (argsIterator.hasNext()) {
            String paramName = trimLeadingSpace(argsIterator.next());
            if (!paramName.isEmpty()) {
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
    }

    private String trimLeadingSpace(String string) {
        return string.replaceAll("^\\s+", "");
    }

    private void setProperty(String fullParamNamePath, Object value) {
        try {
            String[] objectsPath = fullParamNamePath.split("\\.");
            Object targetObject = ensureNestedObjectsInitialization(target, objectsPath);

            String paramName = objectsPath[objectsPath.length - 1];
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(targetObject, paramName);
            ValueWrapper wrappedValue = handleConversion(propertyDescriptor, paramName, value);
            ensureCollectionInitialization(paramName, propertyDescriptor, wrappedValue);
            PropertyUtils.setNestedProperty(targetObject, paramName, wrappedValue.value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"raw"})
    private Object ensureNestedObjectsInitialization(Object mainObject, String[] objectsPath) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        Object target = mainObject;
        for (int i = 0; i < objectsPath.length - 1; i++) {
            String paramName = objectsPath[i];
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(target, paramName);
            Method readMethod = propertyDescriptor.getReadMethod();
            Type genericReturnType = readMethod.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                Type rawType = parameterizedType.getRawType();
                Class<? extends Type> rawTypeClass = rawType.getClass();
                if (rawTypeClass.isInstance(List.class)) {
                    //ensure initialized
                    List list = (List) propertyDescriptor.getReadMethod().invoke(target);
                    if (list == null) {
                        list = new ArrayList();
                        propertyDescriptor.getWriteMethod().invoke(target, list);
                    }
                    int index = resolver.getIndex(paramName);
                    //make sure enough objects exist in list
                    for (int listSize = list.size(); listSize < index + 1; listSize++) {
                        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                        Class<?> aClass1 = Class.forName(actualTypeArgument.getTypeName());
                        Constructor<?> constructor = aClass1.getConstructor();
                        Object o = constructor.newInstance();
                        list.add(o);
                    }
                    target = list.get(index);
                }
            } else {
                Object newTarget = propertyDescriptor.getReadMethod().invoke(target);
                if (newTarget == null) {
                    //make sure nested object is initialized
                    Class<?> aClass = Class.forName(genericReturnType.getTypeName());
                    Constructor<?> constructor = aClass.getConstructor();
                    Object o = constructor.newInstance();
                    propertyDescriptor.getWriteMethod().invoke(target, o);
                    newTarget = o;
                }
                target = newTarget;
            }
        }
        return target;
    }

    @SuppressWarnings({"raw"})
    private void ensureCollectionInitialization(String paramName, PropertyDescriptor propertyDescriptor, ValueWrapper wrappedValue) throws IllegalAccessException, InvocationTargetException {
        if (!wrappedValue.indexed) {
            return;
        }
        if (List.class.equals(wrappedValue.kind)) {
            List list;
            if (propertyDescriptor.getReadMethod().invoke(target) == null) {
                list = new ArrayList();
                propertyDescriptor.getWriteMethod().invoke(target, list);
            } else {
                list = ((List) propertyDescriptor.getReadMethod().invoke(target));
            }
            int size = list.size();
            int index = resolver.getIndex(paramName);
            if (index >= size) {
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
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            if (rawTypeClass.isInstance(List.class)) {
                vw.kind = List.class;
                if (indexed) {
                    //if it's indexed we'll need single value
                    if (isEnum(actualTypeArgument)) {
                        value = convertToEnumValue(value, actualTypeArgument);
                    } else if (isBoolean(actualTypeArgument)) {
                        value = Boolean.parseBoolean(value.toString());
                    } else if (isInteger(actualTypeArgument)) {
                        value = Integer.parseInt(value.toString());
                    }
                } else {
                    value = convertToList(value.toString(), parameterizedType, paramName);
                }
            } else {
                throw new IllegalArgumentException("Unsupported bind type: " + rawTypeClass.getName());
            }
        } else if (isEnum(genericReturnType)) {
            value = convertToEnumValue(value, genericReturnType);
        } else if (isBoolean(readMethod.getReturnType())) {
            value = Boolean.parseBoolean(value.toString());
        } else if (isInteger(readMethod.getReturnType())) {
            value = Integer.parseInt(value.toString());
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
        } else if (isBoolean(actualTypeArguments[0])) {
            return values.stream().map(Boolean::parseBoolean).collect(Collectors.toList());
        } else if (isInteger(actualTypeArguments[0])) {
            return values.stream().map(Integer::parseInt).collect(Collectors.toList());
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

    private boolean isBoolean(Class<?> returnType) {
        return returnType.isAssignableFrom(Boolean.class) || returnType.isAssignableFrom(boolean.class);
    }

    private boolean isInteger(Class<?> returnType) {
        return returnType.isAssignableFrom(Integer.class) || returnType.isAssignableFrom(int.class);
    }

    private boolean isBoolean(Type type) {
        return type instanceof Class<?> && ((Class<?>) type).isAssignableFrom(Boolean.class);
    }

    private boolean isInteger(Type type) {
        return type instanceof Class<?> && ((Class<?>) type).isAssignableFrom(Integer.class);
    }

    //tiny wrapper, so we won't have discover these things repeatedly in each method
    static class ValueWrapper {
        Class<?> kind;
        Object value;
        boolean indexed;
    }
}
