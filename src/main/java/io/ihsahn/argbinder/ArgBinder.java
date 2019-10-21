package io.ihsahn.argbinder;

import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;

public class ArgBinder {

    private Object target;

    public ArgBinder(Object target) {
        this.target = target;
    }

    public void parse(String[] args) {
        Iterator<String> argsIterator = Arrays.asList(args).iterator();
        while (argsIterator.hasNext()) {
            String paramName = argsIterator.next();
            if (argsIterator.hasNext()) {
                String value = argsIterator.next();
                try {
                    PropertyUtils.setNestedProperty(target, paramName, value);
                }  catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
