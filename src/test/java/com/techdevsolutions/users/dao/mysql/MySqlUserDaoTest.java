package com.techdevsolutions.users.dao.mysql;

import com.techdevsolutions.users.beans.auditable.User;
import com.techdevsolutions.users.beans.auditable.UserTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import java.util.List;

@Ignore
public class MySqlUserDaoTest {

    CustomDataSource customDataSource = new CustomDataSource();
    MySqlUserDao dao = new MySqlUserDao(customDataSource.mysqlDataSource());

    public MySqlUserDaoTest() throws Exception {
        try {
            this.dao.install();
        } catch (Exception e) {
            if (!e.getMessage().contains("Unknown table")) {
                throw e;
            }
        }
    }

    @Test
    public void search() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.dao.create(user);
        List<User> items = this.dao.search();
        Assert.assertEquals(1, items.stream().filter((i) -> i.getId().equals(created.getId())).count());
    }

    @Test
    public void get() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.dao.create(user);
        User verify = this.dao.get(created.getId());
        Assert.assertEquals(verify, created);
    }

//    @Test
//    public void create() throws Exception {
//    }

    @Test
    public void remove() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.dao.create(user);
        this.dao.remove(created.getId());

        try {
            this.dao.get(created.getId());
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Incorrect result size: expected 1, actual 0"));
        }
    }

//    @Test
//    public void delete() {
//        // same as remove()
//    }

    @Test
    public void update() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.dao.create(user);

        created.setFirstName("test new first name");
        created.setLastName("test new last name");
        created.setName(created.getFirstName() + " " + created.getLastName());
        created.setEmail("new@test.com");
        created.getTags().add("new tag");
        created.getRoles().add("ADMIN");
        this.dao.update(created);
        User updated = this.dao.get(created.getId());
        Assert.assertEquals(created, updated);
    }

    @Test
    public void verifyRemoval() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.dao.create(user);
        this.dao.remove(created.getId());
        Assert.assertTrue(this.dao.verifyRemoval(created.getId()));
    }

//    @Test
//    public void setupIndex() {
//    }
}
