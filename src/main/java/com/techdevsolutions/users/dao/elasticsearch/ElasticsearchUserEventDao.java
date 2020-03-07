package com.techdevsolutions.users.dao.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.techdevsolutions.common.beans.elasticsearchCommonSchema.Event;
import com.techdevsolutions.common.dao.DaoCrudInterface;
import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.common.service.core.Timer;
import com.techdevsolutions.users.beans.UserCreatedEvent;
import com.techdevsolutions.users.beans.UserEvent;
import com.techdevsolutions.users.beans.UserRemovedEvent;
import com.techdevsolutions.users.beans.UserUpdatedEvent;
import com.techdevsolutions.users.beans.auditable.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ElasticsearchUserEventDao implements DaoCrudInterface<User> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String INDEX_BASE_NAME = "events-users";

    private EventElasticsearchDAO dao;

    @Autowired
    public ElasticsearchUserEventDao(Environment environment) {
        this.dao = new EventElasticsearchDAO("localhost", ElasticsearchUserEventDao.INDEX_BASE_NAME);

        if (environment != null) {
            String elasticsearchHost = environment.getProperty("user.dao.elasticsearch.host");

            if (StringUtils.isNotEmpty(elasticsearchHost)) {
                this.dao = new EventElasticsearchDAO(elasticsearchHost, ElasticsearchUserEventDao.INDEX_BASE_NAME);
            }
        }
    }

    public static UserEvent RemoveUnusedFields(final UserEvent item) throws Exception {
        // Remove fields from the event object that just don't need to be stored
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        UserEvent copy = objectMapper.readValue(objectMapper.writeValueAsString(item), UserEvent.class);
        copy.setSeverity(null);
        copy.setSequence(null);
        copy.setStart(null);
        copy.setEnd(null);
        copy.setTimezone(null);
        copy.setRiskScore(null);
        copy.setRisrScoreNormalized(null);
        copy.setProvider(null);
        copy.setModule(null);
        copy.setOutcome(null);
        copy.setHash(null);
        copy.setOriginal(null);
        return copy;
    }

    @Override
    public List<User> search() {
        return null;
    }

    @Override
    public User get(final String id) throws Exception {
        Timer timer = new Timer().start();

        try {
            if (StringUtils.isEmpty(id)) {
                throw new IllegalArgumentException("id is null or empty");
            }

            Event<?> event = this.dao.getEventByEventDataIdLazy(id, UserEvent.CATEGORY, UserEvent.DATASET);
            UserEvent userEvent = new ObjectMapper().convertValue(event, UserEvent.class);

            if (userEvent.getAction().equals(EventElasticsearchDAO.ACTION_REMOVED)) {
                throw new Exception(("Item has been removed"));
            }

            User item = userEvent.getData();
            this.logger.info("Got item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
            return item;
        } catch (Exception e) {
            this.logger.info("Failed to get item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
            throw e;
        }
    }

    @Override
    public User create(final User item) throws Exception {
        Timer timer = new Timer().start();
        User itemToFind = null;

        try {
            itemToFind = this.get(item.getId());

            if (itemToFind != null) {
                throw new Exception("Item already exists with id: " + item.getId());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Item has been removed")) {
                throw e;
            }
        }

        UserCreatedEvent createdEvent = new UserCreatedEvent(item);
        UserEvent event = ElasticsearchUserEventDao.RemoveUnusedFields(createdEvent);
        this.dao.create(event);
        Thread.sleep(1L); // wait a second because we dont want to the create time to be same as any other
        this.logger.info("Created item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return item;
    }

    @Override
    public void remove(final String id) throws Exception {
        Timer timer = new Timer().start();

        User item = this.get(id);
        UserRemovedEvent removedEvent = new UserRemovedEvent(item);
        UserEvent event = ElasticsearchUserEventDao.RemoveUnusedFields(removedEvent);
        this.dao.create(event);
        this.logger.info("Removed item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
    }

    @Override
    public void delete(final String id) throws Exception {
        // Events are not meant to be deleted
        throw new Exception("Method not implemented: Events are never meant to be deleted. Did you mean to use remove()?");
    }

    @Override
    public User update(final User item) throws Exception {
        Timer timer = new Timer().start();

        // You must use .get(...) to ensure the item hasn't been flagged as removed
        this.get(item.getId());
        UserUpdatedEvent updatedEvent = new UserUpdatedEvent(item);
        UserEvent event = ElasticsearchUserEventDao.RemoveUnusedFields(updatedEvent);
        this.dao.create(event);
        this.logger.info("Updated item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return item;
    }

    public Boolean verifyRemoval(final String id) throws Exception {
        return this.dao.verifyRemoval(id, UserEvent.CATEGORY, UserEvent.DATASET);
    }

    @Override
    public void install() throws Exception {

    }
}
