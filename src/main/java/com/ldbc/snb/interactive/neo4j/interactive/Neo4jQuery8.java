package com.ldbc.snb.interactive.neo4j.interactive;

import com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcQuery8;
import com.ldbc.driver.workloads.ldbc.snb.interactive.LdbcQuery8Result;
import com.ldbc.snb.interactive.neo4j.Domain;

public abstract class Neo4jQuery8<CONNECTION> implements Neo4jQuery<LdbcQuery8, LdbcQuery8Result, CONNECTION> {
    protected static final Integer PERSON_ID = 1;
    protected static final Integer LIMIT = 2;

    /*
    Given a start Person, find (most recent) Comments that are replies to Posts/Comments of the start Person.
    Only consider immediate (1-hop) replies, not the transitive (multi-hop) case.
    Return the top 20 reply Comments, and the Person that created each reply Comment.
    Sort results descending by creation date of reply Comment, and then ascending by identifier of reply Comment.
     */
    protected static final String QUERY_STRING = ""
            + "MATCH (start:" + Domain.Nodes.Person + " {" + Domain.Person.ID + ":{" + PERSON_ID + "}})<-[:" + Domain.Rels.HAS_CREATOR + "]-()"
            + "<-[:" + Domain.Rels.REPLY_OF + "]-(comment:" + Domain.Nodes.Comment + ")-[:" + Domain.Rels.HAS_CREATOR + "]->(person:" + Domain.Nodes.Person + ")\n"
            + "RETURN"
            + " person." + Domain.Person.ID + " AS personId,"
            + " person." + Domain.Person.FIRST_NAME + " AS personFirstName,"
            + " person." + Domain.Person.LAST_NAME + " AS personLastName,"
            + " comment." + Domain.Message.ID + " AS commentId,"
            + " comment." + Domain.Message.CREATION_DATE + " AS commentCreationDate,"
            + " comment." + Domain.Message.CONTENT + " AS commentContent\n"
            + "ORDER BY commentCreationDate DESC, commentId ASC\n"
            + "LIMIT {" + LIMIT + "}";
}
