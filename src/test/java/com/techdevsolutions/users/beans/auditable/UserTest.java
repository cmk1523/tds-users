package com.techdevsolutions.users.beans.auditable;

import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.UUID;

public class UserTest {
    public static User GenerateTestUser() {
        User item = new User();
        item.setId("test-" + UUID.randomUUID().toString());
        item.setName("test user");
        item.setEmail("testuser@gmail.com");
        item.setCreated(123L);
        return item;
    }

    public static User GenerateTestUserV2() {
        User item = new User();
        item.setId("test-456");
        item.setName("John Smith");
        item.setEmail("johnsmith@gmail.com");
        item.setCreated(123456789L);
        return item;
    }

    @Test
    public void test() {
        User item = UserTest.GenerateTestUser();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(item);
        Assert.assertTrue(violations.size() == 0);
    }
}
