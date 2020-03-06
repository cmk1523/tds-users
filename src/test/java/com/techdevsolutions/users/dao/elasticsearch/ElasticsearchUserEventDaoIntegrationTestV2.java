package com.techdevsolutions.users.dao.elasticsearch;

import com.techdevsolutions.common.beans.elasticsearchCommonSchema.Event;
import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.users.beans.auditable.User;
import com.techdevsolutions.users.beans.UserEvent;
import com.techdevsolutions.users.beans.auditable.UserTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class ElasticsearchUserEventDaoIntegrationTestV2 {

    private ElasticsearchUserEventDao dao = new ElasticsearchUserEventDao(null);
    private EventElasticsearchDAO eventElasticsearchDAO =
            new EventElasticsearchDAO("localhost", ElasticsearchUserEventDao.INDEX_BASE_NAME);

    @Test
    public void create() throws Exception {
        User user = UserTest.GenerateTestUserV2();

        List<Event> events = this.eventElasticsearchDAO.getEventsByEventDataId(user.getId(), UserEvent.CATEGORY, UserEvent.DATASET);
        events.forEach((i)->{
            try {
                this.eventElasticsearchDAO.delete(i.getId());
            } catch (Exception ignored) {

            }
        });
        Thread.sleep(1000L);

        User created = this.dao.create(user);
        User get = this.dao.get(created.getId());
        Assert.assertTrue(get.equals(created));
    }
}