package com.techdevsolutions.users.service;

import com.techdevsolutions.common.beans.Search;
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

public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private DaoCrudInterface<User> dao;
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public UserServiceImpl(DaoCrudInterface<User> dao) {
        this.dao = dao;
    }

    @Override
    public List<User> search(Search search) throws Exception {
        return this.dao.search(search);
    }

    @Override
    public List<User> getAll() throws Exception {
        Search search = new Search();
        return this.dao.search(search);
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

        if (item == null) {
            throw new Exception("item is null");
        } else {
            if (item.getCreated() == null) {
                item.setCreated(new Date().getTime());
            }
        }

        Set<ConstraintViolation<User>> violations = this.validator.validate(item);

        if (violations.size() > 0) {
            if (violations.size() != 1 && !violations.toString().contains("must not be blank', propertyPath=id")) {
                throw new Exception("Invalid item: " + violations.toString());
            }
        }

        User created = this.dao.create(item);
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
        this.logger.info("Removed item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
    }

    @Override
    public void delete(String id) throws Exception {
        this.remove(id);
    }

    @Override
    public User update(User item) throws Exception {
        Timer timer = new Timer().start();

        if (item == null) {
            throw new Exception("item is null");
        }

        Set<ConstraintViolation<User>> violations = this.validator.validate(item);

        if (violations.size() > 0) {
            throw new Exception("Invalid item: " + violations.toString());
        }

        User updated = this.dao.update(item);
        this.logger.info("Updated item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return updated;
    }

    @Override
    public void install() throws Exception {
        throw new Exception("Method not implemented");
    }
}
