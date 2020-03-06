package com.techdevsolutions.users.service;

import com.techdevsolutions.common.dao.DaoCrudInterface;
import com.techdevsolutions.common.service.core.Timer;
import com.techdevsolutions.users.beans.auditable.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserEventServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private DaoCrudInterface<User> dao;
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public UserEventServiceImpl(DaoCrudInterface<User> dao) {
        this.dao = dao;
    }

    @Override
    public List<User> search() throws Exception {
        return this.dao.search();
    }

    @Override
    public List<User> getAll() throws Exception {
        return this.dao.search();
    }

    @Override
    public User get(String id) throws Exception {
        Timer timer = new Timer().start();

        if (StringUtils.isEmpty(id)) {
            throw new Exception("id is null or empty");
        }

        User item = this.dao.get(id);
        this.logger.info("Got item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
        return item;
    }

    @Override
    public User create(User item) throws Exception {
        Timer timer = new Timer().start();

        if (item != null) {
            item.setId(UUID.randomUUID().toString());

            if (item.getCreated() == null) {
                item.setCreated(new Date().getTime());
            }
        }

        Set<ConstraintViolation<User>> violations = this.validator.validate(item);

        if (violations.size() > 0) {
            throw new Exception("Invalid item: " + violations.toString());
        }

        User created = this.dao.create(item);
        // This guarantees item is created
        // User created = this.get(item.getId());
        this.logger.info("Created item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return created;
    }

    @Override
    public void remove(String id) throws Exception {
        Timer timer = new Timer().start();

        if (StringUtils.isEmpty(id)) {
            throw new Exception("id is null or empty");
        }

        this.dao.remove(id);
        // This guarantees item is removed
//        this.dao.verifyRemoval(id);
        this.logger.info("Removed item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
    }

    @Override
    public void delete(String id) throws Exception {
        this.remove(id);
    }

    @Override
    public User update(User item) throws Exception {
        Timer timer = new Timer().start();

        Set<ConstraintViolation<User>> violations = this.validator.validate(item);

        if (violations.size() > 0) {
            throw new Exception("Invalid item: " + violations.toString());
        }

        User updated = this.dao.update(item);
        // This guarantees item is created
        // User updated = this.get(item.getId());
        this.logger.info("Updated item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return updated;
    }

    @Override
    public void install() throws Exception {
        throw new Exception("Method not implemented");
    }
}
