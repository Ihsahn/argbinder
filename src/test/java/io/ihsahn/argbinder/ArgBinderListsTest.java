package io.ihsahn.argbinder;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgBinderListsTest {

    @Test
    public void testFlatClassStringValuesAtOnce() {
        FlatClassListsValues target = new FlatClassListsValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"descriptions", "desc1,desc2"};
        binder.parse(args);

        assertEquals(2, target.getDescriptions().size());
        assertEquals("desc1", target.getDescriptions().get(0));
        assertEquals("desc2", target.getDescriptions().get(1));
    }

    @Test
    public void testFlatClassEnumValuesAtOnce() {
        FlatClassListsValues target = new FlatClassListsValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"enums", "valueOne,valueThree"};
        binder.parse(args);

        assertEquals(2, target.getEnums().size());
        assertEquals(SampleEnum.valueOne, target.getEnums().get(0));
        assertEquals(SampleEnum.valueThree, target.getEnums().get(1));
    }

    @Test
    public void testNestedClassStringListValuesAtOnce() {
        UpperLevelForListValues target = new UpperLevelForListValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"values.descriptions", "some description,other description"};
        binder.parse(args);

        assertEquals(2, target.getValues().getDescriptions().size());
        assertEquals("some description", target.getValues().getDescriptions().get(0));
        assertEquals("other description", target.getValues().getDescriptions().get(1));
    }

    @Test
    public void testNestedClassStringListValuesAtOnceWithEqual() {
        UpperLevelForListValues target = new UpperLevelForListValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"values.descriptions=some description,other description"};
        binder.parse(args);

        assertEquals(2, target.getValues().getDescriptions().size());
        assertEquals("some description", target.getValues().getDescriptions().get(0));
        assertEquals("other description", target.getValues().getDescriptions().get(1));
    }

    @Test
    public void testFlatClassStringValuesIndexes() {
        FlatClassListsValues target = new FlatClassListsValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"descriptions[0]", "desc1","descriptions[1]", "desc2"};
        binder.parse(args);

        assertEquals(2, target.getDescriptions().size());
        assertEquals("desc1", target.getDescriptions().get(0));
        assertEquals("desc2", target.getDescriptions().get(1));
    }

    @Test
    public void testFlatClassStringValuesIndexesFail() {
        FlatClassListsValues target = new FlatClassListsValues();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"descriptions[2]", "desc1"};
        assertThrows(Exception.class, () -> {
                    binder.parse(args);
                }
        );
    }

    public enum SampleEnum {
        valueOne, valueTwo, valueThree
    }

    public static class UpperLevelForListValues {

        private FlatClassListsValues values = new FlatClassListsValues();

        public FlatClassListsValues getValues() {
            return values;
        }

        public void setValues(FlatClassListsValues values) {
            this.values = values;
        }
    }

    public static class FlatClassListsValues {
        private List<String> descriptions;
        private List<SampleEnum> enums;

        public List<String> getDescriptions() {
            return descriptions;
        }

        public void setDescriptions(List<String> descriptions) {
            this.descriptions = descriptions;
        }

        public List<SampleEnum> getEnums() {
            return enums;
        }

        public void setEnums(List<SampleEnum> enums) {
            this.enums = enums;
        }
    }

}