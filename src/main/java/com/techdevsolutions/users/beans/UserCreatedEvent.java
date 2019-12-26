package com.techdevsolutions.users.beans;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.users.beans.auditable.User;

public class UserCreatedEvent extends UserEvent {
    public static final String TYPE_CREATED = UserEvent.CATEGORY + ".created";

    public UserCreatedEvent() {
    }

    public UserCreatedEvent(User item) {
        super(item);
        this.setType(UserCreatedEvent.TYPE_CREATED);
        this.setAction(EventElasticsearchDAO.ACTION_CREATED);
        this.setCode(EventElasticsearchDAO.CODE_CREATED);
        this.setKind(EventElasticsearchDAO.KIND_CREATE);
    }

    public UserCreatedEvent(UserEvent item) {
        super(item.getData());
        this.setType(UserCreatedEvent.TYPE_CREATED);
        this.setAction(EventElasticsearchDAO.ACTION_CREATED);
        this.setCode(EventElasticsearchDAO.CODE_CREATED);
        this.setKind(EventElasticsearchDAO.KIND_CREATE);
    }
}
