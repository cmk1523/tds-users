package com.techdevsolutions.users.dao.mysql;

import com.techdevsolutions.common.dao.DaoCrudInterface;
import com.techdevsolutions.common.service.core.DateUtils;
import com.techdevsolutions.users.beans.auditable.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class MySqlUserDao implements DaoCrudInterface<User> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JdbcTemplate jdbcTemplate;
    private UserRowMapper rowMapper = new UserRowMapper();

    @Autowired
    public MySqlUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public MySqlUserDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<User> search() throws Exception {
        String sql = "SELECT id,name,firstName,lastName,email,tags,roles,created " +
                "FROM Users";
//        this.logger.debug("sql: " + sql);
        return jdbcTemplate.query(sql, this.rowMapper);
    }

    @Override
    public User get(String id) throws Exception {
        String sql = "SELECT id,name,firstName,lastName,email,tags,roles,created " +
                "FROM Users " +
                "WHERE id = ?";
//        this.logger.debug("sql: " + sql);
        return this.jdbcTemplate.queryForObject(sql, new Object[] {id}, this.rowMapper);
    }

    @Override
    public User create(User user) throws Exception {
        String sql = "INSERT INTO Users " +
                "(name,firstName,lastName,email,tags,roles,created) " +
                "VALUES (?,?,?,?,?,?,?)";
//        this.logger.debug("sql: " + sql);

        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, user.getName());
                statement.setString(2, user.getFirstName());
                statement.setString(3, user.getLastName());
                statement.setString(4, user.getEmail());
                statement.setString(5, String.join(",", user.getTags()));
                statement.setString(6,String.join(",", user.getRoles()));
                statement.setTimestamp(7, new Timestamp(user.getCreated()));
                return statement;
            }
        }, holder);

        long primaryKey = holder.getKey().longValue();
        return this.get(String.valueOf(primaryKey));
    }

    @Override
    public void remove(String id) throws Exception {
        String sql = "DELETE FROM Users " +
                "WHERE id = ?";
//        this.logger.debug("sql: " + sql);
        Object[] params = {id};
        int rows = this.jdbcTemplate.update(sql, params);
//        this.logger.debug("removed: " + rows + " rows");
    }

    @Override
    public void delete(String id) throws Exception {
        this.remove(id);
    }

    @Override
    public User update(User user) throws Exception {
        String sql = "UPDATE Users " +
                "SET name = ?, " +
                "firstName = ?, " +
                "lastName = ?, " +
                "email = ?, " +
                "tags = ?, " +
                "roles = ?, " +
                "created = ?";
//        this.logger.debug("sql: " + sql);

        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, user.getName());
                statement.setString(2, user.getFirstName());
                statement.setString(3, user.getLastName());
                statement.setString(4, user.getEmail());
                statement.setString(5, String.join(",", user.getTags()));
                statement.setString(6,String.join(",", user.getRoles()));
                statement.setTimestamp(7, new Timestamp(user.getCreated()));
                return statement;
            }
        });

        return this.get(String.valueOf(user.getId()));
    }

    @Override
    public Boolean verifyRemoval(String id) {
        try {
            this.get(id);
        } catch (Exception e) {
            if (e.getMessage().contains("Incorrect result size: expected 1, actual 0")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void install() throws Exception {
        this.dropTable();
        this.createTable();
    }

    public void dropTable() {
        String sql = "DROP TABLE Users";
//        this.logger.debug("sql : " + sql);
        this.jdbcTemplate.execute(sql);
    }

    public void createTable() {
        String sql = "CREATE TABLE Users (" +
                "id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(1024) NOT NULL," +
                "firstName VARCHAR(1024) NOT NULL," +
                "lastName VARCHAR(1024) NOT NULL," +
                "email VARCHAR(1024)," +
                "tags VARCHAR(1024)," +
                "roles VARCHAR(1024)," +
                "created TIMESTAMP" +
                ")";
//        this.logger.debug("sql: " + sql);
        this.jdbcTemplate.execute(sql);
    }
}
