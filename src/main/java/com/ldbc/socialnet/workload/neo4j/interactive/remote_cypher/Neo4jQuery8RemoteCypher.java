package com.ldbc.socialnet.workload.neo4j.interactive.remote_cypher;

import com.ldbc.driver.DbException;
import com.ldbc.driver.workloads.ldbc.socnet.interactive.LdbcQuery8;
import com.ldbc.driver.workloads.ldbc.socnet.interactive.LdbcQuery8Result;
import com.ldbc.socialnet.workload.neo4j.interactive.Neo4jQuery8;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class Neo4jQuery8RemoteCypher extends Neo4jQuery8<Connection> {
    @Override
    public String description() {
        return QUERY_STRING;
    }

    @Override
    public Iterator<LdbcQuery8Result> execute(Connection connection, LdbcQuery8 operation) throws DbException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(QUERY_STRING)) {
            preparedStatement.setLong(PERSON_ID, operation.personId());
            preparedStatement.setInt(LIMIT, operation.limit());
            ResultSet resultSet = preparedStatement.executeQuery();
            return new ResultSetIterator(resultSet);
        } catch (SQLException e) {
            throw new DbException("Error while executing query", e);
        }
    }

    private class ResultSetIterator implements Iterator<LdbcQuery8Result> {
        private final ResultSet resultSet;

        private ResultSetIterator(ResultSet resultSet) {
            this.resultSet = resultSet;
        }

        @Override
        public boolean hasNext() {
            try {
                return resultSet.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public LdbcQuery8Result next() {
            try {
                return new LdbcQuery8Result(
                        resultSet.getLong("personId"),
                        resultSet.getString("personFirstName"),
                        resultSet.getString("personLastName"),
                        resultSet.getLong("commentCreationDate"),
                        resultSet.getLong("commentId"),
                        resultSet.getString("commentContent"));
            } catch (SQLException e) {
                throw new RuntimeException("Error while retrieving next row", e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not supported by " + getClass().getName());
        }
    }
}