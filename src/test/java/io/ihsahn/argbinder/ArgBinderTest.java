package io.ihsahn.argbinder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgBinderTest {

    @Test
    public void testFlatClassStringValues() {
        FlatClassStringValues target = new FlatClassStringValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"name", "some name", "description", "some description"};
        binder.parse(args);

        assertEquals("some name", target.getName());
        assertEquals("some description", target.getDescription());
    }

    @Test
    public void testNestedClassStringValues() {
        UpperLevelClassStringValues target = new UpperLevelClassStringValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"topLevelDescription", "top level description", "values.name", "some name", "values.description", "some description"};
        binder.parse(args);

        assertEquals("top level description", target.getTopLevelDescription());
        assertEquals("some name", target.getValues().getName());
        assertEquals("some description", target.getValues().getDescription());
    }

    @Test
    public void testNestedClassStringValuesWithEqual() {
        UpperLevelClassStringValues target = new UpperLevelClassStringValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"topLevelDescription", "top level description", "values.name=some name", "values.description=some description"};
        binder.parse(args);

        assertEquals("top level description", target.getTopLevelDescription());
        assertEquals("some name", target.getValues().getName());
        assertEquals("some description", target.getValues().getDescription());
    }

    public static class UpperLevelClassStringValues {
        private String topLevelDescription;
        private FlatClassStringValues values = new FlatClassStringValues();

        public String getTopLevelDescription() {
            return topLevelDescription;
        }

        public void setTopLevelDescription(String topLevelDescription) {
            this.topLevelDescription = topLevelDescription;
        }

        public FlatClassStringValues getValues() {
            return values;
        }

        public void setValues(FlatClassStringValues values) {
            this.values = values;
        }
    }

    public static class FlatClassStringValues {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}