package com.techdevsolutions.users.beans;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.users.beans.auditable.User;

public class UserUpdatedEvent extends UserEvent {
    public static final String TYPE_UPDATED = UserEvent.CATEGORY + ".updated";

    public UserUpdatedEvent() {
    }

    public UserUpdatedEvent(User item) {
        super(item);
        this.setType(UserUpdatedEvent.TYPE_UPDATED);
        this.setAction(EventElasticsearchDAO.ACTION_UPDATED);
        this.setCode(EventElasticsearchDAO.CODE_UPDATED);
        this.setKind(EventElasticsearchDAO.KIND_UPDATED);
    }

    public UserUpdatedEvent(UserEvent item) {
        super(item.getData());
        this.setType(UserUpdatedEvent.TYPE_UPDATED);
        this.setAction(EventElasticsearchDAO.ACTION_UPDATED);
        this.setCode(EventElasticsearchDAO.CODE_UPDATED);
        this.setKind(EventElasticsearchDAO.KIND_UPDATED);
    }
}
