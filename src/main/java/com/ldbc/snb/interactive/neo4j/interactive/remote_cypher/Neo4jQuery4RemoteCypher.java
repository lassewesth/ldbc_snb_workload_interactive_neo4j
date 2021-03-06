package com.ldbc.snb.interactive.neo4j.interactive.remote_cypher;

import com.ldbc.driver.DbException;
import com.ldbc.driver.temporal.Duration;
import com.ldbc.driver.temporal.Time;
import com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcQuery4;
import com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcQuery4Result;
import com.ldbc.snb.interactive.neo4j.interactive.Neo4jQuery4;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Neo4jQuery4RemoteCypher extends Neo4jQuery4<Connection> {

    @Override
    public String description() {
        return QUERY_STRING;
    }

    @Override
    public Iterator<LdbcQuery4Result> execute(Connection connection, LdbcQuery4 operation) throws DbException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(QUERY_STRING)) {
            preparedStatement.setLong(PERSON_ID, operation.personId());
            long startDateAsMilli = operation.startDate().getTime();
            int durationHours = operation.durationDays() * 24;
            long endDateAsMilli = Time.fromMilli(startDateAsMilli).plus(Duration.fromHours(durationHours)).asMilli();
            preparedStatement.setLong(MIN_DATE, startDateAsMilli);
            preparedStatement.setLong(MAX_DATE, endDateAsMilli);
            preparedStatement.setInt(LIMIT, operation.limit());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<LdbcQuery4Result> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(resultSetToLdbcQuery4Result(resultSet));
            }
            return results.iterator();
        } catch (SQLException e) {
            throw new DbException("Error while executing query", e);
        }
    }

    private LdbcQuery4Result resultSetToLdbcQuery4Result(ResultSet resultSet) throws SQLException {
        return new LdbcQuery4Result(
                resultSet.getString("tagName"),
                resultSet.getInt("postCount"));
    }
}
