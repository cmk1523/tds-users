package com.techdevsolutions.users.beans;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.users.beans.auditable.User;

public class UserRemovedEvent extends UserEvent {
    public static final String TYPE_REMOVED = UserEvent.CATEGORY + ".removed";

    public UserRemovedEvent() {
    }

    public UserRemovedEvent(User item) {
        super(item);
        this.setType(UserRemovedEvent.TYPE_REMOVED);
        this.setAction(EventElasticsearchDAO.ACTION_REMOVED);
        this.setCode(EventElasticsearchDAO.CODE_REMOVED);
        this.setKind(EventElasticsearchDAO.KIND_REMOVED);
    }

    public UserRemovedEvent(UserEvent item) {
        super(item.getData());
        this.setType(UserRemovedEvent.TYPE_REMOVED);
        this.setAction(EventElasticsearchDAO.ACTION_REMOVED);
        this.setCode(EventElasticsearchDAO.CODE_REMOVED);
        this.setKind(EventElasticsearchDAO.KIND_REMOVED);
    }
}
