package io.ihsahn.argbinder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public enum SampleEnum {
        valueOne, valueTwo, valueThree
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