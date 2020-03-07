package com.techdevsolutions.users.dao.mysql;

import com.techdevsolutions.users.beans.auditable.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

public class UserRowMapper implements RowMapper<User> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User customer = new User();
        customer.setId(String.valueOf(rs.getInt("id")));
        customer.setName(rs.getString("name"));
        customer.setFirstName(rs.getString("firstName"));
        customer.setLastName(rs.getString("lastName"));
        customer.setEmail(rs.getString("email"));
        String tagsStr = rs.getString("tags");
        customer.getTags().addAll(Arrays.asList(tagsStr.split(",")));
        String rolesStr = rs.getString("roles");
        customer.getRoles().addAll(Arrays.asList(rolesStr.split(",")));
        customer.setCreated(rs.getTimestamp("created").getTime());
        return customer;

    }
}
