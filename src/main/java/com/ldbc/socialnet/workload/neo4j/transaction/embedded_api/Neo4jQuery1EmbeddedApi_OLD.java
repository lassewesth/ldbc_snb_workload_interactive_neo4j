package com.ldbc.socialnet.workload.neo4j.transaction.embedded_api;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ldbc.socialnet.workload.Domain;
import com.ldbc.socialnet.workload.LdbcQuery1;
import com.ldbc.socialnet.workload.LdbcQuery1Result;
import com.ldbc.socialnet.workload.neo4j.transaction.Neo4jQuery1;
import com.ldbc.socialnet.workload.neo4j.utils.Utils;

public class Neo4jQuery1EmbeddedApi_OLD implements Neo4jQuery1
{
    @Override
    public String description()
    {
        return "LDBC Query1 Java API Implementation";
    }

    @Override
    public Iterator<LdbcQuery1Result> execute( GraphDatabaseService db, ExecutionEngine engine, LdbcQuery1 params )
    {
        /*
        Given a person’s first name, return up to 10 people with the same first name sorted by last name. 
        Persons are returned (e.g. as for a search page with top 10 shown), 
        and the information is complemented with summaries of the persons' workplaces, places of study, etc.
         */
        List<Node> firstNamePersons = Utils.iteratorToList( db.findNodesByLabelAndProperty( Domain.Node.PERSON,
                Domain.Person.FIRST_NAME, params.firstName() ).iterator() );
        Collections.sort( firstNamePersons, new LastNameComparator() );
        return new ResultIterator( firstNamePersons.iterator() );
    }

    public static class LastNameComparator implements Comparator<Node>
    {
        @Override
        public int compare( Node person1, Node person2 )
        {
            String person1LastName = (String) person1.getProperty( Domain.Person.LAST_NAME );
            String person2LastName = (String) person2.getProperty( Domain.Person.LAST_NAME );
            return person1LastName.compareTo( person2LastName );
        }
    }

    public static class ResultIterator implements Iterator<LdbcQuery1Result>
    {
        private final Iterator<Node> personNodes;

        public ResultIterator( Iterator<Node> personNodes )
        {
            this.personNodes = personNodes;
        }

        @Override
        public boolean hasNext()
        {
            return personNodes.hasNext();
        }

        @Override
        public LdbcQuery1Result next()
        {
            Node person = personNodes.next();
            String firstName = (String) person.getProperty( Domain.Person.FIRST_NAME );
            String lastName = (String) person.getProperty( Domain.Person.LAST_NAME );
            long birthday = (long) person.getProperty( Domain.Person.BIRTHDAY );
            long creationDate = (long) person.getProperty( Domain.Person.CREATION_DATE );
            String gender = (String) person.getProperty( Domain.Person.GENDER );
            String[] languages = (String[]) person.getProperty( Domain.Person.LANGUAGES );
            String browser = (String) person.getProperty( Domain.Person.BROWSER_USED );
            String ip = (String) person.getProperty( Domain.Person.LOCATION_IP );
            String[] emails = (String[]) person.getProperty( Domain.Person.EMAIL_ADDRESSES );
            String personCity = (String) person.getSingleRelationship( Domain.Rel.IS_LOCATED_IN, Direction.OUTGOING ).getEndNode().getProperty(
                    Domain.Place.NAME );
            // (uniCity:CITY)<-[:IS_LOCATED_IN]-(uni:UNIVERSITY)<-[studyAt:STUDY_AT]-(person)
            Collection<String> unis = Lists.newArrayList( Iterables.transform(
                    LdbcTraversers_OLD.personUniversities().traverse( person ), new Function<Path, String>()
                    {
                        @Override
                        public String apply( Path input )
                        {
                            Iterator<Node> nodes = input.nodes().iterator();
                            // skip person
                            nodes.next();
                            Node uni = nodes.next();
                            Node uniCity = nodes.next();
                            Relationship studiedAt = input.relationships().iterator().next();
                            // uni.name, uniCity.name(studyAt.classYear)
                            return String.format( "%s, %s(%s)", uni.getProperty( Domain.Organisation.NAME ),
                                    uniCity.getProperty( Domain.Place.NAME ),
                                    studiedAt.getProperty( Domain.StudiesAt.CLASS_YEAR ) );
                        }
                    } ) );

            // (companyCountry:PLACE:COUNTRY)<-[:IS_LOCATED_IN]-(company:COMPANY)<-[worksAt:WORKS_AT]-(person)
            Collection<String> companies = Lists.newArrayList( Iterables.transform(
                    LdbcTraversers_OLD.personCompanies().traverse( person ), new Function<Path, String>()
                    {
                        @Override
                        public String apply( Path input )
                        {
                            Iterator<Node> nodes = input.nodes().iterator();
                            // skip person
                            nodes.next();
                            Node company = nodes.next();
                            Node companyCountry = nodes.next();
                            Relationship workedAt = input.relationships().iterator().next();
                            // company.name,
                            // companyCountry.name(worksAt.workFrom)
                            return String.format( "%s, %s(%s)", company.getProperty( Domain.Organisation.NAME ),
                                    companyCountry.getProperty( Domain.Place.NAME ),
                                    workedAt.getProperty( Domain.WorksAt.WORK_FROM ) );
                        }
                    } ) );
            return new LdbcQuery1Result( firstName, lastName, birthday, creationDate, gender, languages, browser, ip,
                    emails, personCity, unis, companies );
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

    }
}
