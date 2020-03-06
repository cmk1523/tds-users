package com.techdevsolutions.users.dao.elasticsearch;

import com.techdevsolutions.common.dao.elasticsearch.BaseElasticsearchHighLevel;
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
public class ElasticsearchUserDaoIntegrationTest {

    private ElasticsearchUserDao dao = new ElasticsearchUserDao(null);
    private BaseElasticsearchHighLevel baseElasticsearchHighLevel = new BaseElasticsearchHighLevel("localhost");
    private List<String> ids = new ArrayList<>();

    @After
    public void after() throws InterruptedException {
        this.cleanup();
    }

    public void cleanup() throws InterruptedException {
        System.out.println("Cleaning up...");
        Thread.sleep(3000L);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());

        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"email.keyword\": {\n" +
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

        DeleteByQueryRequest request = new DeleteByQueryRequest(ElasticsearchUserDao.INDEX_BASE_NAME);
        request.setQuery(searchSourceBuilder.query());
        request.setMaxDocs(10000);
        request.setBatchSize(1000);
        request.setScroll(TimeValue.timeValueMinutes(1));
        request.setRefresh(true);

        try {
            this.baseElasticsearchHighLevel.deleteByQuery(request);
        } catch (Exception ignored) {

        }

        System.out.println("Cleaning up... DONE");
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
        User item = UserTest.GenerateTestUser();
        User created = this.dao.create(item);
        this.ids.add(created.getId());
        User verify = this.dao.get(created.getId());
        Assert.assertEquals(verify, created);

        item.setId(null);
        created = this.dao.create(item);
        this.ids.add(created.getId());
        verify = this.dao.get(created.getId());
        Assert.assertEquals(verify, created);
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

        try {
            this.dao.get(created.getId());
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get item by ID"));
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
        Assert.assertEquals(user, created);

        created.setName("test new name");
        this.dao.update(created);
        User updated = this.dao.get(created.getId());
        Assert.assertEquals(created, updated);
    }
}