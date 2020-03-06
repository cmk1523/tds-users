package com.techdevsolutions.users.service;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.users.beans.auditable.User;
import com.techdevsolutions.users.beans.auditable.UserTest;
import com.techdevsolutions.users.dao.elasticsearch.ElasticsearchUserEventDao;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Ignore
public class UserEventServiceImplIntegrationTest {
    private ElasticsearchUserEventDao dao = new ElasticsearchUserEventDao(null);
    private UserEventServiceImpl userService = new UserEventServiceImpl(this.dao);
    private EventElasticsearchDAO eventElasticsearchDAO =
            new EventElasticsearchDAO("localhost", ElasticsearchUserEventDao.INDEX_BASE_NAME);

    private List<String> ids = new ArrayList<>();

    @After
    public void after() throws InterruptedException, IOException {
        this.cleanup();
    }

    public void cleanup() throws InterruptedException, IOException {
        Thread.sleep(3000L);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());

        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"event.data.email.keyword\": {\n" +
                "              \"value\": \"testuser@gmail.com\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        try {
            NamedXContentRegistry namedXContentRegistry = new NamedXContentRegistry(searchModule.getNamedXContents());
            XContent xContent = XContentFactory.xContent(XContentType.JSON);
            XContentParser parser = xContent.createParser(namedXContentRegistry,
                    DeprecationHandler.THROW_UNSUPPORTED_OPERATION, query);
            searchSourceBuilder.parseXContent(parser);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DeleteByQueryRequest request = new DeleteByQueryRequest(ElasticsearchUserEventDao.INDEX_BASE_NAME);
        request.setQuery(searchSourceBuilder.query());
        request.setMaxDocs(10000);
        request.setBatchSize(1000);
        request.setScroll(TimeValue.timeValueMinutes(1));
        request.setRefresh(true);

        try {
            this.eventElasticsearchDAO.deleteByQuery(request);
        } catch (Exception ignored) {

        }
    }

//    @Test
//    public void search() {
//    }
//
//    @Test
//    public void getAll() {
//    }

    @Test
    public void get() throws Exception {
        User item = UserTest.GenerateTestUser();
        item.setCreated(null);
        User created = this.userService.create(item);
        this.ids.add(created.getId());
        Assert.assertTrue(StringUtils.isNotEmpty(created.getId()));
        User verify = this.userService.get(created.getId());
        Assert.assertTrue(created.equals(verify));
    }

//    @Test
//    public void create() throws Exception {
//        // same as get()
//    }

    @Test
    public void remove() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.userService.create(user);
        this.ids.add(created.getId());
        this.userService.remove(user.getId());
//        this.eventElasticsearchDAO.verifyRemoval(user.getId(), UserEvent.CATEGORY, UserEvent.DATASET);
//
//        try {
//            this.userService.get(created.getId());
//            Assert.assertTrue(false);
//        } catch (Exception e) {
//            Assert.assertTrue(e.getMessage().contains("Item has been removed"));
//        }
    }

//    @Test
//    public void delete() {
//        // same as remove()
//    }

    @Test
    public void update() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.userService.create(user);
        this.ids.add(created.getId());
        Assert.assertTrue(user.equals(created));

        created.setName("test new name");
        User updated = this.userService.update(created);
//        this.eventElasticsearchDAO.verifyUpdate(user.getId(), UserEvent.CATEGORY, UserEvent.DATASET);
//
//        User verify = this.userService.get(created.getId());
//        Assert.assertTrue(created.equals(verify));
    }

//    @Test
//    public void install() {
//    }
}