package com.techdevsolutions.users.beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techdevsolutions.common.beans.elasticsearchCommonSchema.Event;
import com.techdevsolutions.users.beans.auditable.User;

import java.util.Date;

public class UserEvent extends Event<User> {
    public static final String CATEGORY = "user";
    public static final String DATASET = "users";

    public UserEvent() {
    }

    public UserEvent(final User item) {
        // Map applicable fields from the source to the event model
        this.setCreated(new Date());
        this.setCategory(UserEvent.CATEGORY);
        this.setDataset(UserEvent.DATASET);
        this.setData(item);

        try {
            this.setOriginal(new ObjectMapper().writeValueAsString(item));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UserEvent setType(String type) {
        super.setType(type);
        return this;
    }

    @Override
    public UserEvent setAction(String action) {
        super.setAction(action);
        return this;
    }

    @Override
    public UserEvent setCode(String code) {
        super.setCode(code);
        return this;
    }

    @Override
    public UserEvent setCreated(Date date) {
        super.setCreated(date);
        return this;
    }

    @Override
    public UserEvent setKind(String kind) {
        super.setKind(kind);
        return this;
    }

}
