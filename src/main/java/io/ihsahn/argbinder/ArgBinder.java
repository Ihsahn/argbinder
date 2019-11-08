package io.ihsahn.argbinder;

import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

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

    @SuppressWarnings("unchecked")
    private void setProperty(String paramName, Object value) {
        try {
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(target, paramName);
            Method readMethod = propertyDescriptor.getReadMethod();
            Type genericReturnType = readMethod.getGenericReturnType();
            if ((genericReturnType instanceof Class && ((Class<?>)genericReturnType).isEnum())) {
                value = Enum.valueOf(((Class<Enum>)genericReturnType), value.toString());
            }
            PropertyUtils.setNestedProperty(target, paramName, value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
