package io.ihsahn.argbinder;

import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
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

            if (value.isPresent()) {
                try {
                    PropertyUtils.setNestedProperty(target, paramName, value.get());
                }  catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
