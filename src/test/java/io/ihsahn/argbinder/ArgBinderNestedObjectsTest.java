package io.ihsahn.argbinder;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArgBinderNestedObjectsTest {

    @Test
    public void testListOfNestedObjectsDepthOne() {
        UpperLevelForSingleDepthNest target = new UpperLevelForSingleDepthNest();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"innerClassesList[0].name=name1", "innerClassesList[0].sampleEnum=valueTwo", "innerClassesList[2].name=name2"};
        binder.parse(args);

        assertEquals(3, target.getInnerClassesList().size());
        assertEquals("name1", target.getInnerClassesList().get(0).getName());
        assertEquals(SampleEnum.valueTwo, target.getInnerClassesList().get(0).getSampleEnum());

        assertNull(target.getInnerClassesList().get(1).getName());
        assertNull(target.getInnerClassesList().get(1).getSampleEnum());

        assertEquals("name2", target.getInnerClassesList().get(2).getName());
        assertNull(target.getInnerClassesList().get(2).getSampleEnum());
    }

    @Test
    public void testListOfNestedObjectsDepthTwo() {
        UpperLevelForTwoDepthNest target = new UpperLevelForTwoDepthNest();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"listAtFirstLevel[0].innerClassesList[0].name=name1", "listAtFirstLevel[1].innerClassesList[0].sampleEnum=valueTwo", "listAtFirstLevel[2].innerClassesList[2].name=name2"};
        binder.parse(args);

        assertEquals(3, target.getListAtFirstLevel().size());

        UpperLevelForSingleDepthNest listElement = target.getListAtFirstLevel().get(0);
        assertNull(listElement.getInnerClassField());
        assertEquals(1, listElement.getInnerClassesList().size());
        InnerClass innerClass = listElement.getInnerClassesList().get(0);
        assertEquals("name1", innerClass.getName());
        assertNull(innerClass.getSampleEnum());

        listElement = target.getListAtFirstLevel().get(1);
        assertNull(listElement.getInnerClassField());
        assertEquals(1, listElement.getInnerClassesList().size());
        innerClass = listElement.getInnerClassesList().get(0);
        assertNull(innerClass.getName());
        assertEquals(SampleEnum.valueTwo, innerClass.getSampleEnum());

        listElement = target.getListAtFirstLevel().get(2);
        assertNull(listElement.getInnerClassField());
        assertEquals(3, listElement.getInnerClassesList().size());
        innerClass = listElement.getInnerClassesList().get(2);
        assertEquals("name2", innerClass.getName());
        assertNull(innerClass.getSampleEnum());
    }

    @Test
    public void testNestedFlatObjectsDepthOne() {
        UpperLevelForSingleDepthNest target = new UpperLevelForSingleDepthNest();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"innerClassField.name=name567", "innerClassField.sampleEnum=valueOne"};
        binder.parse(args);

        assertEquals("name567", target.getInnerClassField().getName());
        assertEquals(SampleEnum.valueOne, target.getInnerClassField().getSampleEnum());
    }

    @Test
    public void testNestedFlatObjectsDepthTwo() {
        UpperLevelForTwoDepthNest target = new UpperLevelForTwoDepthNest();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"field.innerClassField.name=name998", "field.innerClassField.sampleEnum=valueThree"};
        binder.parse(args);

        assertEquals("name998", target.getField().getInnerClassField().getName());
        assertEquals(SampleEnum.valueThree, target.getField().getInnerClassField().getSampleEnum());
    }

    @Test
    public void testBooleans() {
        TopLevelBooleanClass target = new TopLevelBooleanClass();
        ArgBinder binder = new ArgBinder(target);
        String[] args = {"field.bool=true", "children[0].bool=false", "children[1].list=false,true"};
        binder.parse(args);

        assertEquals(Boolean.TRUE, target.getField().getBool());
        assertEquals(2, target.getChildren().size());
        assertEquals(Boolean.FALSE, target.getChildren().get(0).getBool());
        assertEquals(Arrays.asList(Boolean.FALSE, Boolean.TRUE), target.getChildren().get(1).getList());
    }

    public enum SampleEnum {
        valueOne, valueTwo, valueThree
    }

    public static class TopLevelBooleanClass {
        private ChildBooleanClass field;
        private List<ChildBooleanClass> children;

        public ChildBooleanClass getField() {
            return field;
        }

        public void setField(ChildBooleanClass field) {
            this.field = field;
        }

        public List<ChildBooleanClass> getChildren() {
            return children;
        }

        public void setChildren(List<ChildBooleanClass> children) {
            this.children = children;
        }
    }

    public static class ChildBooleanClass {
        private Boolean bool;
        private List<Boolean> list;

        public ChildBooleanClass() {
        }

        public Boolean getBool() {
            return bool;
        }

        public void setBool(Boolean bool) {
            this.bool = bool;
        }

        public List<Boolean> getList() {
            return list;
        }

        public void setList(List<Boolean> list) {
            this.list = list;
        }
    }
    public static class UpperLevelForTwoDepthNest {
        private SomeClassWithInnerClass field;
        private List<UpperLevelForSingleDepthNest> listAtFirstLevel;

        public SomeClassWithInnerClass getField() {
            return field;
        }

        public void setField(SomeClassWithInnerClass field) {
            this.field = field;
        }

        public List<UpperLevelForSingleDepthNest> getListAtFirstLevel() {
            return listAtFirstLevel;
        }

        public void setListAtFirstLevel(List<UpperLevelForSingleDepthNest> listAtFirstLevel) {
            this.listAtFirstLevel = listAtFirstLevel;
        }
    }

    public static class SomeClassWithInnerClass {
        private InnerClass innerClassField;

        public SomeClassWithInnerClass() {
        }

        public InnerClass getInnerClassField() {
            return innerClassField;
        }

        public void setInnerClassField(InnerClass innerClassField) {
            this.innerClassField = innerClassField;
        }
    }

    public static class UpperLevelForSingleDepthNest {

        private List<InnerClass> innerClassesList;
        private InnerClass innerClassField;

        public UpperLevelForSingleDepthNest() {
        }

        public List<InnerClass> getInnerClassesList() {
            return innerClassesList;
        }

        public void setInnerClassesList(List<InnerClass> innerClassesList) {
            this.innerClassesList = innerClassesList;
        }

        public InnerClass getInnerClassField() {
            return innerClassField;
        }

        public void setInnerClassField(InnerClass innerClassField) {
            this.innerClassField = innerClassField;
        }
    }

    public static class InnerClass {
        private String name;
        private SampleEnum sampleEnum;

        public InnerClass() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public SampleEnum getSampleEnum() {
            return sampleEnum;
        }

        public void setSampleEnum(SampleEnum sampleEnum) {
            this.sampleEnum = sampleEnum;
        }
    }
}
