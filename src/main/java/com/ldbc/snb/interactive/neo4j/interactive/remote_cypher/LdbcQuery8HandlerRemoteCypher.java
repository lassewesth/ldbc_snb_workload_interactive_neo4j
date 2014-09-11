package com.ldbc.snb.interactive.neo4j.interactive.remote_cypher;

import com.google.common.collect.ImmutableList;
import com.ldbc.driver.DbException;
import com.ldbc.driver.OperationHandler;
import com.ldbc.driver.OperationResultReport;
import com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcQuery8;
import com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcQuery8Result;
import com.ldbc.snb.interactive.neo4j.Neo4jConnectionState;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LdbcQuery8HandlerRemoteCypher extends OperationHandler<LdbcQuery8> {
    private static final Neo4jQuery8RemoteCypher query = new Neo4jQuery8RemoteCypher();

    @Override
    protected OperationResultReport executeOperation(LdbcQuery8 operation) throws DbException {
        Connection connection;
        try {
            connection = ((Neo4jConnectionState) dbConnectionState()).connection();
        } catch (SQLException e) {
            throw new DbException("Error while getting connection", e);
        }
        List<LdbcQuery8Result> result = null;
        int resultCode = 0;
        try {
            result = ImmutableList.copyOf(query.execute(connection, operation));
        } catch (Throwable e) {
            resultCode = 1;
        }
        return operation.buildResult(resultCode, result);
    }
}
