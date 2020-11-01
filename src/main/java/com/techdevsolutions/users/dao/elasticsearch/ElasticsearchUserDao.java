package com.techdevsolutions.users.dao.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.techdevsolutions.common.beans.Search;
import com.techdevsolutions.common.beans.elasticsearchCommonSchema.Event;
import com.techdevsolutions.common.dao.DaoCrudInterface;
import com.techdevsolutions.common.dao.elasticsearch.BaseElasticsearchHighLevel;
import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.common.service.core.Timer;
import com.techdevsolutions.users.beans.UserCreatedEvent;
import com.techdevsolutions.users.beans.UserEvent;
import com.techdevsolutions.users.beans.UserRemovedEvent;
import com.techdevsolutions.users.beans.UserUpdatedEvent;
import com.techdevsolutions.users.beans.auditable.User;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ElasticsearchUserDao implements DaoCrudInterface<User> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String INDEX_BASE_NAME = "users";

    private BaseElasticsearchHighLevel dao;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ElasticsearchUserDao(Environment environment) {
        this.dao = new BaseElasticsearchHighLevel("localhost");

        if (environment != null) {
            String elasticsearchHost = environment.getProperty("user.dao.elasticsearch.host");

            if (StringUtils.isNotEmpty(elasticsearchHost)) {
                this.dao = new BaseElasticsearchHighLevel(elasticsearchHost);
            }
        }
    }

    @Override
    public List<User> search(Search search) throws Exception {
        // return this.dao.getDocuments();
        return null;
    }

    private User tryAndGetItem(String id) throws Exception {
        int maxCount = 3;
        int count = 0;

        while(count < maxCount) {
            try {
                User user = this.get(id);

                if (user != null) {
                    return user;
                }
            } catch (Exception ignored) {}

            try {
                Thread.sleep(500 * (count + 1));
                count++;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public User get(final String id) throws Exception {
        Timer timer = new Timer().start();

        try {
            if (StringUtils.isEmpty(id)) {
                throw new IllegalArgumentException("id is null or empty");
            }

            GetResponse getResponse = this.dao.getDocument(id, ElasticsearchUserDao.INDEX_BASE_NAME);

            if (getResponse.isExists()) {
                User item = this.objectMapper.convertValue(getResponse.getSource(), User.class);
                item.setId(getResponse.getId());
                this.logger.info("Got item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
                return item;
            } else {
                throw new Exception("Failed to get item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
            }
        } catch (Exception e) {
            this.logger.info("Failed to get item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
            throw e;
        }
    }

    @Override
    public User create(final User item) throws Exception {
        Timer timer = new Timer().start();
        User itemToFind = null;

        if (!StringUtils.isEmpty(item.getId())) {
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
        }

        String itemAsStr = this.objectMapper.writeValueAsString(item);

        String itemId = item.getId();

        if (StringUtils.isEmpty(itemId)) {
            itemId = this.dao.createDocument(itemAsStr, ElasticsearchUserDao.INDEX_BASE_NAME);
        } else {
            this.dao.createDocument(itemAsStr, item.getId(), ElasticsearchUserDao.INDEX_BASE_NAME);
        }

        User newItem = this.tryAndGetItem(itemId);
        if (newItem == null) {
            throw new Exception("Unable to create item. Unable to verify item exists.");
        }

        this.logger.info("Created item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return newItem;
    }

    @Override
    public void remove(final String id) throws Exception {
        Timer timer = new Timer().start();

        try {
            this.dao.deleteDocument(id, ElasticsearchUserDao.INDEX_BASE_NAME);
        } catch (Exception ignored) {}

        this.logger.info("Removed item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
    }

    @Override
    public void delete(final String id) throws Exception {
        this.delete(id);
    }

    @Override
    public User update(final User item) throws Exception {
        Timer timer = new Timer().start();

        User existingItem = this.get(item.getId());

        if (existingItem == null) {
            throw new Exception("Item doens't exist by id: " + item.getId());
        }

        String itemAsStr = this.objectMapper.writeValueAsString(item);
        this.dao.updateDocument(itemAsStr, item.getId(), ElasticsearchUserDao.INDEX_BASE_NAME);
        this.logger.info("Updated item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return item;
    }

    @Override
    public Boolean verifyRemoval(String id) throws Exception {
        return this.tryAndGetItem(id) == null;
    }

    @Override
    public void install() throws Exception {
        throw new Exception("Method not implemented yet");
    }
}
