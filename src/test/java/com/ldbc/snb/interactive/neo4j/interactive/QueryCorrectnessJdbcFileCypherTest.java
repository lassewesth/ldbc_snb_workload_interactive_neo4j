package com.ldbc.snb.interactive.neo4j.interactive;

import com.google.common.collect.Lists;
import com.ldbc.driver.DbException;
import com.ldbc.driver.Operation;
import com.ldbc.driver.workloads.ldbc.snb.interactive.*;
import com.ldbc.snb.interactive.neo4j.TestUtils;
import com.ldbc.snb.interactive.neo4j.interactive.remote_cypher.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.jdbc.Neo4jConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class QueryCorrectnessJdbcFileCypherTest extends QueryCorrectnessTest<JdbcFileConnectionState> {
    private <OPERATION_RESULT, OPERATION extends Operation<List<OPERATION_RESULT>>> Iterator<OPERATION_RESULT> executeQuery(
            OPERATION operation,
            Neo4jQuery<OPERATION, OPERATION_RESULT, Connection> query,
            Connection connection) throws DbException {
        // TODO uncomment to print query
        System.out.println(operation.toString() + "\n" + query.description() + "\n");
        return query.execute(connection, operation);
    }

    @Override
    public JdbcFileConnectionState openConnection(String path) throws Exception {
        GraphDatabaseService db;
        Connection connection;
        try {
            db = new GraphDatabaseFactory()
                    .newEmbeddedDatabaseBuilder(path)
                    .loadPropertiesFromFile(TestUtils.getResource("/neo4j_run_dev.properties").getAbsolutePath())
                    .newGraphDatabase();
            Properties properties = new Properties();
            properties.put("mydb" + path, db);
            connection = DriverManager.getConnection("jdbc:neo4j:instance:mydb" + path, properties);
        } catch (Throwable e) {
            throw new DbException("Could not create database connection", e);
        }
        return new JdbcFileConnectionState(connection, db);
    }

    @Override
    public void closeConnection(JdbcFileConnectionState connection) throws Exception {
        connection.connection().close();
        connection.db().shutdown();
    }

    @Override
    public Iterator<LdbcQuery1Result> neo4jQuery1Impl(JdbcFileConnectionState connection, LdbcQuery1 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery1RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery2Result> neo4jQuery2Impl(JdbcFileConnectionState connection, LdbcQuery2 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery2RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery3Result> neo4jQuery3Impl(JdbcFileConnectionState connection, LdbcQuery3 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery3RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery4Result> neo4jQuery4Impl(JdbcFileConnectionState connection, LdbcQuery4 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery4RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery5Result> neo4jQuery5Impl(JdbcFileConnectionState connection, LdbcQuery5 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery5RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery6Result> neo4jQuery6Impl(JdbcFileConnectionState connection, LdbcQuery6 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery6RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery7Result> neo4jQuery7Impl(JdbcFileConnectionState connection, LdbcQuery7 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery7RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery8Result> neo4jQuery8Impl(JdbcFileConnectionState connection, LdbcQuery8 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery8RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery9Result> neo4jQuery9Impl(JdbcFileConnectionState connection, LdbcQuery9 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery9RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery10Result> neo4jQuery10Impl(JdbcFileConnectionState connection, LdbcQuery10 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery10RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery11Result> neo4jQuery11Impl(JdbcFileConnectionState connection, LdbcQuery11 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery11RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery12Result> neo4jQuery12Impl(JdbcFileConnectionState connection, LdbcQuery12 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery12RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery13Result> neo4jQuery13Impl(JdbcFileConnectionState connection, LdbcQuery13 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery13RemoteCypher(), connection.connection());
    }

    @Override
    public Iterator<LdbcQuery14Result> neo4jQuery14Impl(JdbcFileConnectionState connection, LdbcQuery14 operation) throws Exception {
        return executeQuery(operation, new Neo4jQuery14RemoteCypher(), connection.connection());
    }
}