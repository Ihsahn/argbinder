package io.ihsahn.argbinder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArgBinderTest {

    @Test
    public void testFlatClassStringAndEnumValues() {
        FlatClassStringValues target = new FlatClassStringValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"name", "some name", "description", "some description", "sampleEnum", "valueOne"};
        binder.parse(args);

        assertEquals("some name", target.getName());
        assertEquals("some description", target.getDescription());
        assertEquals(SampleEnum.valueOne, target.getSampleEnum());
    }

    @Test
    public void testFlatClassBoolValues() {
        FlatClassBoolValues target = new FlatClassBoolValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"booleanObjectProperty", "true", "booleanPrimitiveProperty", "false"};
        binder.parse(args);

        assertEquals(Boolean.TRUE, target.getBooleanObjectProperty());
        assertEquals(false, target.isBooleanPrimitiveProperty());
    }

    @Test
    public void testFlatClassIntValues() {
        FlatClassIntegerValues target = new FlatClassIntegerValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"intObjectProperty", "1", "intPrimitiveProperty", "2"};
        binder.parse(args);

        assertEquals(1, target.getIntObjectProperty());
        assertEquals(2, target.getIntPrimitiveProperty());
    }

    @Test
    public void testNestedClassStringAndEnumValues() {
        UpperLevelClassStringValues target = new UpperLevelClassStringValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"topLevelDescription", "top level description", "values.name", "some name",
                "values.description", "some description", "values.sampleEnum", "valueTwo"};
        binder.parse(args);

        assertEquals("top level description", target.getTopLevelDescription());
        assertEquals("some name", target.getValues().getName());
        assertEquals("some description", target.getValues().getDescription());
        assertEquals(SampleEnum.valueTwo, target.getValues().getSampleEnum());
    }

    @Test
    public void testNestedClassBoolValues() {
        UpperLevelClassBoolValues target = new UpperLevelClassBoolValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"booleanObjectProperty", "true", "values.booleanObjectProperty", "false",
                "values.booleanPrimitiveProperty", "true"};
        binder.parse(args);

        assertEquals(Boolean.TRUE, target.getBooleanObjectProperty());
        assertEquals(false, target.isBooleanPrimitiveProperty());
        assertEquals(Boolean.FALSE, target.getValues().getBooleanObjectProperty());
        assertEquals(true, target.getValues().isBooleanPrimitiveProperty());
    }

    @Test
    public void testNestedIntValues() {
        UpperLevelClassIntegerValues target = new UpperLevelClassIntegerValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"intObjectProperty", "3", "values.intObjectProperty", "4",
                "values.intPrimitiveProperty", "5"};
        binder.parse(args);

        assertEquals(3, target.getIntObjectProperty());
        assertEquals(0, target.getIntPrimitiveProperty()); //primitive, defaults to 0
        assertEquals(4, target.getValues().getIntObjectProperty());
        assertEquals(5, target.getValues().getIntPrimitiveProperty());
    }

    @Test
    public void testNestedClassStringAndEnumValuesWithEqual() {
        UpperLevelClassStringValues target = new UpperLevelClassStringValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"topLevelDescription", "top level description", "values.name=some name",
                "values.description=some description", "values.sampleEnum=valueThree"};
        binder.parse(args);

        assertEquals("top level description", target.getTopLevelDescription());
        assertEquals("some name", target.getValues().getName());
        assertEquals("some description", target.getValues().getDescription());
        assertEquals(SampleEnum.valueThree, target.getValues().getSampleEnum());
    }

    @Test
    public void testEmptyParamsAndLeadingSpaces() {
        FlatClassStringValues target = new FlatClassStringValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"", " ", "  name", "some name", "description", "some description", "sampleEnum", "valueOne"};
        binder.parse(args);

        assertEquals("some name", target.getName());
        assertEquals("some description", target.getDescription());
        assertEquals(SampleEnum.valueOne, target.getSampleEnum());
    }

    public enum SampleEnum {
        valueOne, valueTwo, valueThree
    }

    public static class UpperLevelClassIntegerValues {
        private Integer intObjectProperty;
        private int intPrimitiveProperty;

        private FlatClassIntegerValues values = new FlatClassIntegerValues();

        public Integer getIntObjectProperty() {
            return intObjectProperty;
        }

        public void setIntObjectProperty(Integer intObjectProperty) {
            this.intObjectProperty = intObjectProperty;
        }

        public int getIntPrimitiveProperty() {
            return intPrimitiveProperty;
        }

        public void setIntPrimitiveProperty(int intPrimitiveProperty) {
            this.intPrimitiveProperty = intPrimitiveProperty;
        }

        public FlatClassIntegerValues getValues() {
            return values;
        }

        public void setValues(FlatClassIntegerValues values) {
            this.values = values;
        }
    }

    public static class UpperLevelClassBoolValues {
        private Boolean booleanObjectProperty;
        private boolean booleanPrimitiveProperty;

        private FlatClassBoolValues values = new FlatClassBoolValues();

        public FlatClassBoolValues getValues() {
            return values;
        }

        public void setValues(FlatClassBoolValues values) {
            this.values = values;
        }

        public Boolean getBooleanObjectProperty() {
            return booleanObjectProperty;
        }

        public void setBooleanObjectProperty(Boolean booleanObjectProperty) {
            this.booleanObjectProperty = booleanObjectProperty;
        }

        public boolean isBooleanPrimitiveProperty() {
            return booleanPrimitiveProperty;
        }

        public void setBooleanPrimitiveProperty(boolean booleanPrimitiveProperty) {
            this.booleanPrimitiveProperty = booleanPrimitiveProperty;
        }
    }

    public static class UpperLevelClassStringValues {
        private String topLevelDescription;
        private SampleEnum topLevelEnum;

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

        public SampleEnum getTopLevelEnum() {
            return topLevelEnum;
        }

        public void setTopLevelEnum(SampleEnum topLevelEnum) {
            this.topLevelEnum = topLevelEnum;
        }
    }

    public static class FlatClassIntegerValues {
        private Integer intObjectProperty;
        private int intPrimitiveProperty;

        public Integer getIntObjectProperty() {
            return intObjectProperty;
        }

        public void setIntObjectProperty(Integer intObjectProperty) {
            this.intObjectProperty = intObjectProperty;
        }

        public int getIntPrimitiveProperty() {
            return intPrimitiveProperty;
        }

        public void setIntPrimitiveProperty(int intPrimitiveProperty) {
            this.intPrimitiveProperty = intPrimitiveProperty;
        }
    }

    public static class FlatClassBoolValues {
        private Boolean booleanObjectProperty;
        private boolean booleanPrimitiveProperty;

        public Boolean getBooleanObjectProperty() {
            return booleanObjectProperty;
        }

        public void setBooleanObjectProperty(Boolean booleanObjectProperty) {
            this.booleanObjectProperty = booleanObjectProperty;
        }

        public boolean isBooleanPrimitiveProperty() {
            return booleanPrimitiveProperty;
        }

        public void setBooleanPrimitiveProperty(boolean booleanPrimitiveProperty) {
            this.booleanPrimitiveProperty = booleanPrimitiveProperty;
        }

    }

    public static class FlatClassStringValues {
        private String name;
        private String description;
        private SampleEnum sampleEnum;

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

        public SampleEnum getSampleEnum() {
            return sampleEnum;
        }

        public void setSampleEnum(SampleEnum sampleEnum) {
            this.sampleEnum = sampleEnum;
        }
    }
}