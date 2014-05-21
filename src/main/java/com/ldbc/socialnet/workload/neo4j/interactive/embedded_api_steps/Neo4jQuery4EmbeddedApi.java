package com.ldbc.socialnet.workload.neo4j.interactive.embedded_api_steps;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.ldbc.driver.workloads.ldbc.socnet.interactive.LdbcQuery4;
import com.ldbc.driver.workloads.ldbc.socnet.interactive.LdbcQuery4Result;
import com.ldbc.socialnet.workload.neo4j.Domain;
import com.ldbc.socialnet.workload.neo4j.interactive.LdbcTraversers;
import com.ldbc.socialnet.workload.neo4j.interactive.Neo4jQuery4;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.traversal.steps.execution.StepsUtils;

import java.util.*;
import java.util.Map.Entry;

public class Neo4jQuery4EmbeddedApi extends Neo4jQuery4<GraphDatabaseService> {
    private final LdbcTraversers traversers;

    public Neo4jQuery4EmbeddedApi(LdbcTraversers traversers) {
        this.traversers = traversers;
    }

    @Override
    public String description() {
        return "LDBC Query4 Java API Implementation";
    }

    @Override
    public Iterator<LdbcQuery4Result> execute(GraphDatabaseService db, LdbcQuery4 operation) {
        /*
        Find the top 10 most popular topics/tags (by the number of comments and posts) that your friends have 
        been talking about in the last x hours.
        
        MATCH (person:PERSON)-[:KNOWS]-(friend:PERSON)
        USING INDEX person:PERSON(id)
        WHERE person.id={person_id}
        WITH friend
        MATCH (friend)<-[:HAS_CREATOR]-(post:POST)
        WHERE post.creationDate>={min_date} AND post.creationDate<={max_date}
        WITH post
        MATCH (post)-[HAS_TAG]->(tag:TAG)
        WITH DISTINCT tag, collect(tag) AS tags
        RETURN tag.name AS tagName, length(tags) AS tagCount
        ORDER BY tagCount DESC
        LIMIT 10         
        */
        Iterator<Node> personIterator = db.findNodesByLabelAndProperty(Domain.Nodes.Person, Domain.Person.ID,
                operation.personId()).iterator();
        if (false == personIterator.hasNext()) return Iterators.emptyIterator();
        Node person = personIterator.next();
        Iterator<String> tagNames = Iterators.transform(
                traversers.friendPostTags(operation.minDateAsMilli(), operation.maxDateAsMilli()).traverse(person).nodes().iterator(),
                new Function<Node, String>() {
                    @Override
                    public String apply(Node endNode) {
                        return (String) endNode.getProperty(Domain.Tag.NAME);
                    }
                });
        Map<String, Integer> tagNamesCountMap = StepsUtils.count(tagNames);
        List<LdbcQuery4Result> tagCounts = Lists.newArrayList(Iterables.transform(tagNamesCountMap.entrySet(),
                new Function<Entry<String, Integer>, LdbcQuery4Result>() {
                    @Override
                    public LdbcQuery4Result apply(Entry<String, Integer> input) {
                        return new LdbcQuery4Result(input.getKey(), input.getValue());
                    }
                }));
        Collections.sort(tagCounts, new TagCountComparator());
        return Iterators.limit(tagCounts.iterator(), 10);
    }

    public static class TagCountComparator implements Comparator<LdbcQuery4Result> {
        @Override
        public int compare(LdbcQuery4Result result1, LdbcQuery4Result result2) {
            if (result1.tagCount() == result2.tagCount()) return 0;
            if (result1.tagCount() > result2.tagCount()) return -1;
            return 1;
        }
    }
}
