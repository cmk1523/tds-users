package com.techdevsolutions.users.dao.elasticsearch;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.users.beans.auditable.User;
import com.techdevsolutions.users.beans.auditable.UserTest;
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
public class ElasticsearchUserEventDaoIntegrationTest {

    private ElasticsearchUserEventDao dao = new ElasticsearchUserEventDao(null);
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
//    public void removeUnusedFields() {
//    }
//
//    @Test
//    public void getByDocumentId() {
//    }
//
//    @Test
//    public void getEventsByMessageId() {
//    }
//
//    @Test
//    public void search() {
//    }

    @Test
    public void get() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.dao.create(user);
        this.ids.add(created.getId());
        User get = this.dao.get(created.getId());
        Assert.assertTrue(get.equals(created));
    }

//    @Test
//    public void create() throws Exception {
//        // same as get()
//    }

    @Test
    public void remove() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.dao.create(user);
        this.ids.add(created.getId());
        this.dao.remove(user.getId());
//        this.eventElasticsearchDAO.verifyRemoval(user.getId(), UserEvent.CATEGORY, UserEvent.DATASET);
//
//        try {
//            this.dao.get(created.getId());
//            Assert.assertTrue(false);
//        } catch (Exception e) {
//            Assert.assertTrue(e.getMessage().contains("Item has been removed"));
//        }
    }

    @Test
    public void delete() {
        User user = UserTest.GenerateTestUser();

        try {
            this.dao.delete(user.getId());
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Method not implemented: Events are never meant to be deleted. " +
                    "Did you mean to use remove()?"));
        }
    }

    @Test
    public void update() throws Exception {
        User user = UserTest.GenerateTestUser();
        User created = this.dao.create(user);
        Assert.assertTrue(user.equals(created));

        created.setName("test new name");
        this.dao.update(created);
//        this.eventElasticsearchDAO.verifyUpdate(user.getId(), UserEvent.CATEGORY, UserEvent.DATASET);
//
//        User updated = this.dao.get(created.getId());
//        Assert.assertTrue(created.equals(updated));
    }
}