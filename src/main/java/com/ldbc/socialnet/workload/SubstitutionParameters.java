package com.ldbc.socialnet.workload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.FileUtils;
import org.neo4j.traversal.steps.execution.StepsUtils;

import com.google.common.base.Function;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.ldbc.socialnet.workload.neo4j.utils.Config;

import static com.ldbc.socialnet.workload.Domain.*;

public class SubstitutionParameters
{
    public static void main( String[] args ) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        File parametersFile = new File( "parameters.json" );
        File countryPairsFile = new File( "countryPairs.txt" );
        FileUtils.deleteRecursively( parametersFile );
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( Config.DB_DIR ).setConfig(
                Config.NEO4J_RUN_CONFIG ).newGraphDatabase();
        try
        {
            SubstitutionParameters parametersWrite = SubstitutionParameters.populate( db, countryPairsFile );
            System.out.println( mapper.writeValueAsString( parametersWrite ) );
            mapper.writeValue( parametersFile, parametersWrite );

            SubstitutionParameters parametersRead = SubstitutionParameters.fromJson( parametersFile );
            System.out.println( mapper.writeValueAsString( parametersRead ) );

            System.out.println( mapper.writeValueAsString( parametersRead ).equals(
                    mapper.writeValueAsString( parametersWrite ) ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            db.shutdown();
        }
    }

    /*
    DETAILS
     - http://www.ldbc.eu:8090/display/TUC/IW+Substitution+parameters+selection
    
    GENERATE
     - personNames.txt
     - personNumber.txt
     - creationPostDate.txt 
         0,33,66,100 percents of date range... or maybe all 0..100
         duration of date range
     - tagUris.txt
     - countryUris.txt (orgLocations.txt)
     - workFromDate.txt
     - tagClassUris.txt
      
    PROVIDED 
     - countryPairs.txt
     */

    @JsonProperty( value = COUNTRY_PAIRS )
    public List<String[]> countryPairs = null;
    @JsonProperty( value = FIRST_NAMES )
    public List<String> firstNames = null;
    @JsonProperty( value = POST_CREATION_DATES )
    public Map<Integer, Long> postCreationDates = null;
    @JsonProperty( value = PERSON_IDS )
    public List<Long> personIds = null;
    @JsonProperty( value = TAG_URIS )
    public List<String> tagUris = null;
    @JsonProperty( value = HOROSCOPE_SIGNS )
    public List<Integer> horoscopeSigns = null;
    @JsonProperty( value = COUNTRY_URIS )
    public List<String> countryUris = null;
    @JsonProperty( value = WORK_FROM_DATES )
    public List<Integer> workFromDates = null;
    @JsonProperty( value = TAG_CLASS_URIS )
    public List<String> tagClassUris = null;

    private SubstitutionParameters()
    {
    }

    private static final String COUNTRY_PAIRS = "countryPairs";
    private static final String FIRST_NAMES = "first_names";
    private static final String POST_CREATION_DATES = "post_creation_dates";
    private static final String PERSON_IDS = "person_ids";
    private static final String TAG_URIS = "tag_uris";
    private static final String HOROSCOPE_SIGNS = "horoscope_signs";
    private static final String COUNTRY_URIS = "country_uris";
    private static final String WORK_FROM_DATES = "work_from_dates";
    private static final String TAG_CLASS_URIS = "tag_class_uris";

    public static SubstitutionParameters populate( GraphDatabaseService db, File countryPairsFile )
            throws FileNotFoundException
    {
        SubstitutionParameters parameters = new SubstitutionParameters();
        ExecutionEngine engine = new ExecutionEngine( db );
        parameters.countryPairs = countryPairs( countryPairsFile );
        parameters.firstNames = firstNames( engine );
        parameters.postCreationDates = postCreationDates( engine );
        parameters.personIds = personIds( engine );
        parameters.tagUris = tagUris( engine );
        parameters.horoscopeSigns = horoscopeSigns();
        parameters.countryUris = countryUris( engine );
        parameters.workFromDates = workFromDates( engine );
        parameters.tagClassUris = tagClassUris( engine );
        return parameters;
    }

    public static SubstitutionParameters fromJson( File jsonFile ) throws JsonParseException, JsonMappingException,
            IOException
    {
        return new ObjectMapper().readValue( jsonFile, SubstitutionParameters.class );
    }

    private static List<String[]> countryPairs( File countryPairsFile ) throws FileNotFoundException
    {
        CountryPairsFileReader countryPairsReader = new CountryPairsFileReader( countryPairsFile );
        return ImmutableList.copyOf( countryPairsReader );
    }

    private static List<String> firstNames( ExecutionEngine engine )
    {
        String query = "MATCH (person:" + Nodes.Person + ")\n"

        + "RETURN person." + Person.FIRST_NAME + " AS name";

        return ImmutableList.copyOf( StepsUtils.distinct( Iterables.transform( engine.execute( query ),
                new Function<Map<String, Object>, String>()
                {
                    @Override
                    public String apply( Map<String, Object> result )
                    {
                        return (String) result.get( "name" );
                    }
                } ) ) );
    }

    private static Map<Integer, Long> postCreationDates( ExecutionEngine engine )
    {
        String query = "MATCH (post:" + Nodes.Post + ")\n"

        + "WITH post." + Post.CREATION_DATE + " AS date\n"

        + "RETURN date\n"

        + "ORDER BY date DESC";

        List<Long> creationDates = ImmutableList.copyOf( Iterables.transform( engine.execute( query ),
                new Function<Map<String, Object>, Long>()
                {
                    @Override
                    public Long apply( Map<String, Object> result )
                    {
                        return (long) result.get( "date" );
                    }
                } ) );

        final long percent100CreationDateAsMilli = creationDates.get( 0 );
        final long percent0CreationDateAsMilli = creationDates.get( creationDates.size() - 1 );
        final long range = percent100CreationDateAsMilli - percent0CreationDateAsMilli;
        final long onePercentOfRange = range / 100;

        Map<Integer, Long> creationDateRangePercentages = new HashMap<Integer, Long>();
        for ( Integer percentage : ContiguousSet.create( Range.closed( 0, 100 ), DiscreteDomain.integers() ) )
        {
            long creationDateRangePercentage = percent0CreationDateAsMilli + ( onePercentOfRange * percentage );
            creationDateRangePercentages.put( percentage, creationDateRangePercentage );
        }
        return creationDateRangePercentages;
    }

    private static List<Long> personIds( ExecutionEngine engine )
    {
        String query = "MATCH (person:" + Nodes.Person + ")\n"

        + "RETURN person." + Person.ID + " AS id";

        return ImmutableList.copyOf( StepsUtils.distinct( Iterables.transform( engine.execute( query ),
                new Function<Map<String, Object>, Long>()
                {
                    @Override
                    public Long apply( Map<String, Object> result )
                    {
                        return (long) result.get( "id" );
                    }
                } ) ) );
    }

    private static List<String> tagUris( ExecutionEngine engine )
    {
        String query = "MATCH (tag:" + Nodes.Tag + ")\n"

        + "RETURN tag." + Tag.URI + " AS uri";

        return ImmutableList.copyOf( StepsUtils.distinct( Iterables.transform( engine.execute( query ),
                new Function<Map<String, Object>, String>()
                {
                    @Override
                    public String apply( Map<String, Object> result )
                    {
                        return (String) result.get( "uri" );
                    }
                } ) ) );
    }

    private static List<Integer> horoscopeSigns()
    {
        return ImmutableList.copyOf( ContiguousSet.create( Range.closed( 1, 12 ), DiscreteDomain.integers() ) );
    }

    private static List<String> countryUris( ExecutionEngine engine )
    {
        String query = "MATCH (country:" + Place.Type.Country + ")\n"

        + "RETURN country." + Place.URI + " AS uri";

        return ImmutableList.copyOf( StepsUtils.distinct( Iterables.transform( engine.execute( query ),
                new Function<Map<String, Object>, String>()
                {
                    @Override
                    public String apply( Map<String, Object> result )
                    {
                        return (String) result.get( "uri" );
                    }
                } ) ) );
    }

    private static List<Integer> workFromDates( ExecutionEngine engine )
    {
        String query = "MATCH (:" + Nodes.Person + ")-[workFrom:" + Rels.WORKS_AT + "]->()\n"

        + "RETURN workFrom." + WorksAt.WORK_FROM + " AS workFrom";

        return ImmutableList.copyOf( StepsUtils.distinct( Iterables.transform( engine.execute( query ),
                new Function<Map<String, Object>, Integer>()
                {
                    @Override
                    public Integer apply( Map<String, Object> result )
                    {
                        return (Integer) result.get( "workFrom" );
                    }
                } ) ) );
    }

    private static List<String> tagClassUris( ExecutionEngine engine )
    {
        String query = "MATCH (tagClass:" + Nodes.TagClass + ")\n"

        + "RETURN tagClass." + TagClass.URI + " AS uri";

        return ImmutableList.copyOf( StepsUtils.distinct( Iterables.transform( engine.execute( query ),
                new Function<Map<String, Object>, String>()
                {
                    @Override
                    public String apply( Map<String, Object> result )
                    {
                        return (String) result.get( "uri" );
                    }
                } ) ) );
    }

    static class CountryPairsFileReader implements Iterator<String[]>
    {
        private final Pattern columnSeparatorPattern = Pattern.compile( "\\ " );

        private final BufferedReader bufferedReader;

        private String[] next = null;
        private boolean closed = false;

        public CountryPairsFileReader( File countryPairsFile ) throws FileNotFoundException
        {
            this.bufferedReader = new BufferedReader( new FileReader( countryPairsFile ) );
        }

        @Override
        public boolean hasNext()
        {
            if ( true == closed ) return false;
            next = ( next == null ) ? nextCountryPair() : next;
            if ( null == next ) closed = closeReader();
            return ( null != next );
        }

        @Override
        public String[] next()
        {
            next = ( null == next ) ? nextCountryPair() : next;
            if ( null == next ) throw new NoSuchElementException( "No more lines to read" );
            String[] tempNext = next;
            next = null;
            return tempNext;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        private String[] nextCountryPair()
        {
            String line = null;
            try
            {
                line = bufferedReader.readLine();
                if ( null == line ) return null;
                String[] separatedLine = columnSeparatorPattern.split( line, -1 );
                if ( separatedLine.length == 2 )
                    return separatedLine;
                else
                    throw new RuntimeException( "Unexpected line encountered: " + line );
            }
            catch ( IOException e )
            {
                String errMsg = String.format( "Error retrieving next csv entry from file [%s]", bufferedReader );
                throw new RuntimeException( errMsg, e.getCause() );
            }
        }

        private boolean closeReader()
        {
            if ( true == closed )
            {
                String errMsg = "Can not close file multiple times";
                throw new RuntimeException( errMsg );
            }
            if ( null == bufferedReader )
            {
                String errMsg = "Can not close file - reader is null";
                throw new RuntimeException( errMsg );
            }
            try
            {
                bufferedReader.close();
            }
            catch ( IOException e )
            {
                String errMsg = String.format( "Error closing file [%s]", bufferedReader );
                throw new RuntimeException( errMsg, e.getCause() );
            }
            return true;
        }
    }
}
