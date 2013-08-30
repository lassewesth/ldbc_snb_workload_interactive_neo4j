package com.ldbc.socialnet.workload.neo4j.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import com.ldbc.socialnet.workload.Domain;
import com.ldbc.socialnet.workload.neo4j.load.tempindex.TempIndexFactory;
import com.ldbc.socialnet.workload.neo4j.utils.Utils;

public class LdbcSocialNetworkCsvFileInserters
{
    private static final Logger logger = Logger.getLogger( LdbcSocialNetworkCsvFileInserters.class );

    private final static Map<String, Object> EMPTY_MAP = new HashMap<String, Object>();
    private final static String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat( DATE_TIME_FORMAT_STRING );
    private final static String DATE_FORMAT_STRING = "yyyy-MM-dd";
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( DATE_FORMAT_STRING );

    public static List<CsvFileInserter> all( TempIndexFactory<Long, Long> tempIndexFactory,
            BatchInserter batchInserter, String csvDataDir ) throws FileNotFoundException
    {
        /*
        * Neo4j Batch Index Providers
        */
        CommentsBatchIndex commentsIndex = new CommentsBatchIndex( tempIndexFactory.create() );
        PostsBatchIndex postsIndex = new PostsBatchIndex( tempIndexFactory.create() );
        PersonsBatchIndex personsIndex = new PersonsBatchIndex( tempIndexFactory.create() );
        ForumsBatchIndex forumsIndex = new ForumsBatchIndex( tempIndexFactory.create() );
        TagsBatchIndex tagsIndex = new TagsBatchIndex( tempIndexFactory.create() );
        TagClassesBatchIndex tagClassesIndex = new TagClassesBatchIndex( tempIndexFactory.create() );
        OrganisationsBatchIndex organisationsIndex = new OrganisationsBatchIndex( tempIndexFactory.create() );
        LanguagesBatchIndex languagesIndex = new LanguagesBatchIndex( tempIndexFactory.create() );
        PlacesBatchIndex placesIndex = new PlacesBatchIndex( tempIndexFactory.create() );
        EmailAddressesBatchIndex emailAddressesIndex = new EmailAddressesBatchIndex( tempIndexFactory.create() );

        /*
        * CSV Files
        */
        List<CsvFileInserter> fileInserters = new ArrayList<CsvFileInserter>();
        fileInserters.add( comments( csvDataDir, batchInserter, commentsIndex ) );
        fileInserters.add( forums( csvDataDir, batchInserter, forumsIndex ) );
        fileInserters.add( organisations( csvDataDir, batchInserter, organisationsIndex ) );
        fileInserters.add( persons( csvDataDir, batchInserter, personsIndex ) );
        fileInserters.add( places( csvDataDir, batchInserter, placesIndex ) );
        fileInserters.add( posts( csvDataDir, batchInserter, postsIndex ) );
        fileInserters.add( tagClasses( csvDataDir, batchInserter, tagClassesIndex ) );
        fileInserters.add( tags( csvDataDir, batchInserter, tagsIndex ) );
        fileInserters.add( commentHasCreatorPerson( csvDataDir, batchInserter, personsIndex, commentsIndex ) );
        fileInserters.add( commentIsLocatedInPlace( csvDataDir, batchInserter, commentsIndex, placesIndex ) );
        fileInserters.add( commentReplyOfComment( csvDataDir, batchInserter, commentsIndex ) );
        fileInserters.add( commentReplyOfPost( csvDataDir, batchInserter, commentsIndex, postsIndex ) );
        fileInserters.add( forumContainerOfPost( csvDataDir, batchInserter, forumsIndex, postsIndex ) );
        fileInserters.add( forumHasMemberPerson( csvDataDir, batchInserter, forumsIndex, personsIndex ) );
        fileInserters.add( forumHasModeratorPerson( csvDataDir, batchInserter, personsIndex, forumsIndex ) );
        fileInserters.add( forumHasTag( csvDataDir, batchInserter, forumsIndex, tagsIndex ) );
        fileInserters.add( personHasEmailAddress( csvDataDir, batchInserter, personsIndex, emailAddressesIndex ) );
        fileInserters.add( personHasInterestTag( csvDataDir, batchInserter, personsIndex, tagsIndex ) );
        fileInserters.add( personIsLocatedInPlace( csvDataDir, batchInserter, personsIndex, placesIndex ) );
        fileInserters.add( personKnowsPerson( csvDataDir, batchInserter, personsIndex ) );
        fileInserters.add( personLikesPost( csvDataDir, batchInserter, personsIndex, postsIndex ) );
        fileInserters.add( personSpeaksLanguage( csvDataDir, batchInserter, personsIndex, languagesIndex ) );
        fileInserters.add( personStudyAtOrganisation( csvDataDir, batchInserter, personsIndex, organisationsIndex ) );
        fileInserters.add( personWorksAtOrganisation( csvDataDir, batchInserter, personsIndex, organisationsIndex ) );
        fileInserters.add( placeIsPartOfPlace( csvDataDir, batchInserter, placesIndex ) );
        fileInserters.add( postHasCreatorPerson( csvDataDir, batchInserter, personsIndex, postsIndex ) );
        fileInserters.add( postHasTagTag( csvDataDir, batchInserter, postsIndex, tagsIndex ) );
        fileInserters.add( postIsLocatedInPlace( csvDataDir, batchInserter, postsIndex, placesIndex ) );
        fileInserters.add( tagClassIsSubclassOfTagClass( csvDataDir, batchInserter, tagClassesIndex ) );
        fileInserters.add( tagHasTypeTagClass( csvDataDir, batchInserter, tagsIndex, tagClassesIndex ) );
        fileInserters.add( organisationBasedNearPlace( csvDataDir, batchInserter, organisationsIndex, placesIndex ) );

        return fileInserters;
    }

    private static CsvFileInserter comments( final String csvDataDir, final BatchInserter batchInserter,
            final CommentsBatchIndex commentsIndex ) throws FileNotFoundException
    {
        /*
        id  creationDate            location IP     browserUsed     content
        00  2010-03-11T10:11:18Z    14.134.0.11     Chrome          About Michael Jordan, Association...
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.COMMENT ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                long id = Long.parseLong( (String) columnValues[0] );
                // TODO remove?
                // properties.put( Domain.Comment.ID, id );
                String creationDateString = (String) columnValues[1];
                try
                {
                    // 2010-12-28T07:16:25Z
                    Date creationDate = DATE_TIME_FORMAT.parse( creationDateString );
                    properties.put( Domain.Comment.CREATION_DATE, creationDate.getTime() );
                }
                catch ( ParseException e )
                {
                    long now = System.currentTimeMillis();
                    properties.put( Domain.Comment.CREATION_DATE, now );
                    logger.error( String.format( "Invalid DateTime string: %s\nSet creationDate to now instead\n%s",
                            creationDateString, e ) );
                }
                properties.put( Domain.Comment.LOCATION_IP, columnValues[2] );
                properties.put( Domain.Comment.BROWSER_USED, columnValues[3] );
                properties.put( Domain.Comment.CONTENT, columnValues[4] );
                long commentNodeId = batchInserter.createNode( properties, Domain.Node.COMMENT );
                commentsIndex.put( id, commentNodeId );
            }
        } );
    }

    private static CsvFileInserter posts( final String csvDataDir, final BatchInserter batchInserter,
            final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        id      imageFile   creationDate            locationIP      browserUsed     language    content
        100     photo9.jpg  2010-03-11T05:28:04Z    27.99.128.8     Firefox         zh          About Michael Jordan...
        */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.POST ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                long id = Long.parseLong( (String) columnValues[0] );
                // TODO remove?
                // properties.put( "id", id );
                properties.put( Domain.Post.IMAGE_FILE, columnValues[1] );
                String creationDateString = (String) columnValues[2];
                try
                {
                    // 2010-12-28T07:16:25Z
                    Date creationDate = DATE_TIME_FORMAT.parse( creationDateString );
                    properties.put( Domain.Post.CREATION_DATE, creationDate.getTime() );
                }
                catch ( ParseException e )
                {
                    long now = System.currentTimeMillis();
                    properties.put( Domain.Post.CREATION_DATE, now );
                    logger.error( String.format( "Invalid DateTime string: %s\nSet creationDate to now instead\n%s",
                            creationDateString, e ) );
                }
                properties.put( Domain.Post.LOCATION_IP, columnValues[3] );
                properties.put( Domain.Post.BROWSER_USED, columnValues[4] );
                properties.put( Domain.Post.LANGUAGE, columnValues[5] );
                properties.put( Domain.Post.CONTENT, columnValues[6] );
                long postNodeId = batchInserter.createNode( properties, Domain.Node.POST );
                postsIndex.put( id, postNodeId );
            }
        } );
    }

    private static CsvFileInserter persons( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex ) throws FileNotFoundException
    {
        /*
        id      firstName   lastName    gender  birthday    creationDate            locationIP      browserUsed
        75      Fernanda    Alves       male    1984-12-15  2010-12-14T11:41:37Z    143.106.0.7     Firefox
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                long id = Long.parseLong( (String) columnValues[0] );
                // TODO remove?
                properties.put( Domain.Person.ID, id );
                properties.put( Domain.Person.FIRST_NAME, columnValues[1] );
                properties.put( Domain.Person.LAST_NAME, columnValues[2] );
                properties.put( Domain.Person.GENDER, columnValues[3] );
                String birthdayString = (String) columnValues[4];
                try
                {
                    // 1984-12-15
                    Date birthday = DATE_FORMAT.parse( birthdayString );
                    properties.put( Domain.Person.BIRTHDAY, birthday.getTime() );
                }
                catch ( ParseException e )
                {
                    long now = System.currentTimeMillis();
                    properties.put( Domain.Person.BIRTHDAY, now );
                    logger.error( String.format( "Invalid Date string: %s\nSet birthday to now instead\n%s",
                            birthdayString, e ) );
                }
                String creationDateString = (String) columnValues[5];
                try
                {
                    // 2010-12-28T07:16:25Z
                    Date creationDate = DATE_TIME_FORMAT.parse( creationDateString );
                    properties.put( Domain.Person.CREATION_DATE, creationDate.getTime() );
                }
                catch ( ParseException e )
                {
                    long now = System.currentTimeMillis();
                    properties.put( Domain.Person.CREATION_DATE, now );
                    logger.error( String.format( "Invalid DateTime string: %s\nSet creationDate to now instead\n%s",
                            creationDateString, e ) );
                }
                properties.put( Domain.Person.LOCATION_IP, columnValues[6] );
                properties.put( Domain.Person.BROWSER_USED, columnValues[7] );
                long personNodeId = batchInserter.createNode( properties, Domain.Node.PERSON );
                personsIndex.put( id, personNodeId );
            }
        } );
    }

    private static CsvFileInserter forums( final String csvDataDir, final BatchInserter batchInserter,
            final ForumsBatchIndex forumIndex ) throws FileNotFoundException
    {
        /*
            id      title                       creationDate
            150     Wall of Fernanda Alves      2010-12-14T11:41:37Z
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.FORUM ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                long id = Long.parseLong( (String) columnValues[0] );
                // TODO remove?
                // properties.put( "id", id );
                properties.put( Domain.Forum.TITLE, columnValues[1] );
                String creationDateString = (String) columnValues[2];
                try
                {
                    // 2010-12-28T07:16:25Z
                    Date creationDate = DATE_TIME_FORMAT.parse( creationDateString );
                    properties.put( Domain.Forum.CREATION_DATE, creationDate.getTime() );
                }
                catch ( ParseException e )
                {
                    long now = System.currentTimeMillis();
                    properties.put( Domain.Forum.CREATION_DATE, now );
                    logger.error( String.format( "Invalid DateTime string: %s\nSet creationDate to now instead\n%s",
                            creationDateString, e ) );
                }
                long forumNodeId = batchInserter.createNode( properties, Domain.Node.FORUM );
                forumIndex.put( id, forumNodeId );
            }
        } );
    }

    private static CsvFileInserter tags( final String csvDataDir, final BatchInserter batchInserter,
            final TagsBatchIndex tagIndex ) throws FileNotFoundException
    {
        /*
        id      name                url
        259     Gilberto_Gil        http://dbpedia.org/resource/Gilberto_Gil
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.TAG ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                long id = Long.parseLong( (String) columnValues[0] );
                // TODO remove?
                // properties.put( "id", id );
                properties.put( Domain.Tag.NAME, columnValues[1] );
                properties.put( Domain.Tag.URL, columnValues[2] );
                long tagNodeId = batchInserter.createNode( properties, Domain.Node.TAG );
                tagIndex.put( id, tagNodeId );
            }
        } );
    }

    private static CsvFileInserter tagClasses( final String csvDataDir, final BatchInserter batchInserter,
            final TagClassesBatchIndex tagClassesIndex ) throws FileNotFoundException
    {
        /*
        id      name    url
        211     Person  http://dbpedia.org/ontology/Person
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.TAGCLASS ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                long id = Long.parseLong( (String) columnValues[0] );
                // TODO remove?
                // properties.put( "id", id );
                properties.put( Domain.TagClass.NAME, columnValues[1] );
                properties.put( Domain.TagClass.URL, columnValues[2] );
                long tagClassNodeId = batchInserter.createNode( properties, Domain.Node.TAG_CLASS );
                tagClassesIndex.put( id, tagClassNodeId );
            }
        } );
    }

    private static CsvFileInserter organisations( final String csvDataDir, final BatchInserter batchInserter,
            final OrganisationsBatchIndex organisationsIndex ) throws FileNotFoundException
    {
        /*
        id  type        name                        url
        00  university  Universidade_de_Pernambuco  http://dbpedia.org/resource/Universidade_de_Pernambuco
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.ORGANISATION ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                long id = Long.parseLong( (String) columnValues[0] );
                // TODO remove?
                // properties.put( "id", id );
                properties.put( Domain.Organisation.NAME, columnValues[2] );
                // TODO only necessary if connecting to dbpedia
                // properties.put( "url", columnValues[3] );
                long organisationNodeId = batchInserter.createNode( properties, Domain.Node.ORGANISATION,
                        Domain.Organisation.Type.valueOf( ( (String) columnValues[1] ).toUpperCase() ) );
                organisationsIndex.put( id, organisationNodeId );
            }
        } );
    }

    private static CsvFileInserter places( final String csvDataDir, final BatchInserter batchInserter,
            final PlacesBatchIndex placeIndex ) throws FileNotFoundException
    {
        /*
        id      name            url                                             type
        5170    South_America   http://dbpedia.org/resource/South_America       REGION
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PLACE ), new CsvLineInserter()
        {
            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                long id = Long.parseLong( (String) columnValues[0] );
                // TODO remove?
                // properties.put( "id", id );
                properties.put( Domain.Place.NAME, columnValues[1] );
                properties.put( Domain.Place.URL, columnValues[2] );
                Domain.Place.Type placeType = Domain.Place.Type.valueOf( ( (String) columnValues[3] ).toUpperCase() );
                long placeNodeId = batchInserter.createNode( properties, Domain.Node.PLACE, placeType );
                placeIndex.put( id, placeNodeId );
            }
        } );
    }

    private static CsvFileInserter commentReplyOfComment( final String csvDataDir, final BatchInserter batchInserter,
            final CommentsBatchIndex commentsIndex ) throws FileNotFoundException
    {
        /*
        Comment.id  Comment.id
        20          00
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.COMMENT_REPLY_OF_COMMENT ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromCommentNodeId = commentsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long toCommentNodeId = commentsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { fromCommentNodeId, toCommentNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.REPLY_OF,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter commentReplyOfPost( final String csvDataDir, final BatchInserter batchInserter,
            final CommentsBatchIndex commentsIndex, final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        Comment.id  Post.id
        00          100
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.COMMENT_REPLY_OF_POST ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromCommentNodeId = commentsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long toPostNodeId = postsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { fromCommentNodeId, toPostNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.REPLY_OF,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter commentIsLocatedInPlace( final String csvDataDir, final BatchInserter batchInserter,
            final CommentsBatchIndex commentsIndex, final PlacesBatchIndex placesIndex ) throws FileNotFoundException
    {
        /*
        Comment.id  Place.id
        100         73
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.COMMENT_LOCATED_IN_PLACE ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long commentNodeId = commentsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long placeNodeId = placesIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { commentNodeId, placeNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.IS_LOCATED_IN, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter placeIsPartOfPlace( final String csvDataDir, final BatchInserter batchInserter,
            final PlacesBatchIndex placesIndex ) throws FileNotFoundException
    {
        /*
        Place.id Place.id
        11          5170
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PLACE_IS_PART_OF_PLACE ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromPlaceNodeId = placesIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long toPlaceNodeId = placesIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { fromPlaceNodeId, toPlaceNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.IS_PART_OF, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personKnowsPerson( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex ) throws FileNotFoundException
    {
        /*
        Person.id   Person.id
        75          1489
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON_KNOWS_PERSON ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromPersonNodeId = personsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long toPersonNodeId = personsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { fromPersonNodeId, toPersonNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.KNOWS,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personStudyAtOrganisation( final String csvDataDir,
            final BatchInserter batchInserter, final PersonsBatchIndex personsIndex,
            final OrganisationsBatchIndex organisationsIndex ) throws FileNotFoundException
    {
        /*
        Person.id   Organisation.id classYear
        75          00                  2004
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON_STUDIES_AT_ORGANISATION ),
                new CsvLineInserter()
                {
                    @Override
                    public Object[] transform( Object[] columnValues )
                    {
                        long fromPersonNodeId = personsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                        long toOrganisationNodeId = organisationsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                        int classYear = Integer.parseInt( (String) columnValues[2] );
                        return new Object[] { fromPersonNodeId, toOrganisationNodeId, classYear };
                    }

                    @Override
                    public void insert( Object[] columnValues )
                    {
                        Map<String, Object> properties = new HashMap<String, Object>();
                        properties.put( Domain.StudiesAt.CLASS_YEAR, columnValues[2] );
                        batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                                Domain.Rel.STUDY_AT, properties );
                    }
                } );
    }

    private static CsvFileInserter personSpeaksLanguage( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final LanguagesBatchIndex languagesIndex )
            throws FileNotFoundException
    {
        /*        
        Person.id   language
        75          pt
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON_SPEAKS_LANGUAGE ), new CsvLineInserter()
        {
            /*
             * TODO file needs to have better format
             * updating nodes, especially resizing arrays, does not encourage vendors to load big datasets
             */
            @Override
            public void insert( Object[] columnValues )
            {
                long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                Map<String, Object> personNodeProperties = batchInserter.getNodeProperties( personNodeId );
                String[] languages = (String[]) personNodeProperties.get( Domain.Person.LANGUAGES );
                String newLanguages = (String) columnValues[1];
                String[] langaugesPlusNewLanguage = Utils.copyArrayAndAddElement( languages, newLanguages );
                batchInserter.setNodeProperty( personNodeId, Domain.Person.LANGUAGES, langaugesPlusNewLanguage );
            }
        } );
    }

    private static CsvFileInserter commentHasCreatorPerson( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final CommentsBatchIndex commentsIndex ) throws FileNotFoundException
    {
        /*        
        Comment.id  Person.id
        00          1402
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.COMMENT_HAS_CREATOR_PERSON ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long commentNodeId = commentsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { commentNodeId, personNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_CREATOR, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter postHasCreatorPerson( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        Post.id     Person.id
        00          75
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.POST_HAS_CREATOR_PERSON ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long postNodeId = postsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { postNodeId, personNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_CREATOR, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter forumHasModeratorPerson( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final ForumsBatchIndex forumsIndex ) throws FileNotFoundException
    {
        /*
        Forum.id    Person.id
        1500        75
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.FORUM_HAS_MODERATOR_PERSON ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long forumNodeId = forumsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { forumNodeId, personNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_MODERATOR, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personIsLocatedInPlace( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final PlacesBatchIndex placesIndex ) throws FileNotFoundException
    {
        /*        
        Person.id   Place.id
        75          310
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON_IS_LOCATED_IN_PLACE ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long placeNodeId = placesIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { personNodeId, placeNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.IS_LOCATED_IN, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personWorksAtOrganisation( final String csvDataDir,
            final BatchInserter batchInserter, final PersonsBatchIndex personsIndex,
            final OrganisationsBatchIndex organisationsIndex ) throws FileNotFoundException
    {
        /*
        Person.id   Organisation.id     workFrom
        75          10                  2016
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON_WORKS_AT_ORGANISATION ),
                new CsvLineInserter()
                {
                    @Override
                    public Object[] transform( Object[] columnValues )
                    {
                        long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                        long organisationNodeId = organisationsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                        int workFrom = Integer.parseInt( (String) columnValues[2] );
                        return new Object[] { personNodeId, organisationNodeId, workFrom };
                    }

                    @Override
                    public void insert( Object[] columnValues )
                    {
                        Map<String, Object> properties = new HashMap<String, Object>();
                        properties.put( Domain.WorksAt.WORK_FROM, columnValues[2] );
                        batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                                Domain.Rel.WORKS_AT, properties );
                    }
                } );
    }

    private static CsvFileInserter personHasInterestTag( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final TagsBatchIndex tagsIndex ) throws FileNotFoundException
    {
        /*
        Person.id   Tag.id
        75          259
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON_HAS_INTEREST_TAG ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long tagNodeId = tagsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { personNodeId, tagNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_INTEREST, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personHasEmailAddress( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final EmailAddressesBatchIndex emailAddressesIndex )
            throws FileNotFoundException
    {
        /*
        Person.id   email
        75          Fernanda75@gmx.com
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON_EMAIL_ADDRESS ), new CsvLineInserter()
        {
            /*
             * TODO file needs to have better format
             * updating nodes, especially resizing arrays, does not encourage vendors to load big datasets
             */
            @Override
            public void insert( Object[] columnValues )
            {
                long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                Map<String, Object> personNodeProperties = batchInserter.getNodeProperties( personNodeId );
                String[] emailAddresses = (String[]) personNodeProperties.get( Domain.Person.EMAIL_ADDRESSES );
                String newEmailAddress = (String) columnValues[1];
                String[] emailAddressPlusNewAddress = Utils.copyArrayAndAddElement( emailAddresses, newEmailAddress );
                batchInserter.setNodeProperty( personNodeId, Domain.Person.EMAIL_ADDRESSES, emailAddressPlusNewAddress );
            }
        } );
    }

    private static CsvFileInserter postHasTagTag( final String csvDataDir, final BatchInserter batchInserter,
            final PostsBatchIndex postsIndex, final TagsBatchIndex tagsIndex ) throws FileNotFoundException
    {
        /*
        Post.id Tag.id
        100     2903
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.POST_HAS_TAG_TAG ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long postNodeId = postsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long tagNodeId = tagsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { postNodeId, tagNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.HAS_TAG,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter personLikesPost( final String csvDataDir, final BatchInserter batchInserter,
            final PersonsBatchIndex personsIndex, final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        Person.id   Post.id     creationDate
        1489        00          2011-01-20T11:18:41Z
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.PERSON_LIKES_POST ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long fromPersonNodeId = personsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long toPostNodeId = postsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                String creationDateString = (String) columnValues[2];
                long creationDateAsTime;
                try
                {
                    // 2010-12-28T07:16:25Z
                    creationDateAsTime = DATE_TIME_FORMAT.parse( creationDateString ).getTime();
                }
                catch ( ParseException e )
                {
                    long now = System.currentTimeMillis();
                    creationDateAsTime = now;
                    logger.error( String.format( "Invalid DateTime string: %s\nSet creationDate to now instead\n%s",
                            creationDateString, e ) );
                }
                return new Object[] { fromPersonNodeId, toPostNodeId, creationDateAsTime };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put( Domain.Likes.CREATION_DATE, columnValues[2] );
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.LIKES,
                        properties );
            }
        } );
    }

    private static CsvFileInserter postIsLocatedInPlace( final String csvDataDir, final BatchInserter batchInserter,
            final PostsBatchIndex postsIndex, final PlacesBatchIndex placesIndex ) throws FileNotFoundException
    {
        /*
        Post.id     Place.id
        00          11
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.POST_IS_LOCATED_IN_PLACE ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long postNodeId = postsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long placeNodeId = placesIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { postNodeId, placeNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.IS_LOCATED_IN, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter forumHasMemberPerson( final String csvDataDir, final BatchInserter batchInserter,
            final ForumsBatchIndex forumsIndex, final PersonsBatchIndex personsIndex ) throws FileNotFoundException
    {
        /*
        Forum.id    Person.id   joinDate
        150         1489        2011-01-02T01:01:10Z        
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.FORUM_HAS_MEMBER_PERSON ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long forumNodeId = forumsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long personNodeId = personsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                String joinDateString = (String) columnValues[2];
                long joinDateAsTime;
                try
                {
                    // 2010-12-28T07:16:25Z
                    joinDateAsTime = DATE_TIME_FORMAT.parse( joinDateString ).getTime();
                }
                catch ( ParseException e )
                {
                    long now = System.currentTimeMillis();
                    joinDateAsTime = now;
                    logger.error( String.format( "Invalid DateTime string: %s\nSet joinDate to now instead\n%s",
                            joinDateString, e ) );
                }
                return new Object[] { forumNodeId, personNodeId, joinDateAsTime };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put( Domain.HasMember.JOIN_DATE, columnValues[2] );
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.HAS_MEMBER, properties );
            }
        } );
    }

    private static CsvFileInserter forumContainerOfPost( final String csvDataDir, final BatchInserter batchInserter,
            final ForumsBatchIndex forumsIndex, final PostsBatchIndex postsIndex ) throws FileNotFoundException
    {
        /*
        Forum.id    Post.id
        40220       00
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.FORUMS_CONTAINER_OF_POST ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long forumNodeId = forumsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long postNodeId = postsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { forumNodeId, postNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                        Domain.Rel.CONTAINER_OF, EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter forumHasTag( final String csvDataDir, final BatchInserter batchInserter,
            final ForumsBatchIndex forumsIndex, final TagsBatchIndex tagsIndex ) throws FileNotFoundException
    {
        /*
        Forum.id    Tag.id
        75          259
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.FORUM_HAS_TAG_TAG ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long forumNodeId = forumsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long tagNodeId = tagsIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { tagNodeId, forumNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.HAS_TAG,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter tagHasTypeTagClass( final String csvDataDir, final BatchInserter batchInserter,
            final TagsBatchIndex tagsIndex, final TagClassesBatchIndex tagClassesIndex ) throws FileNotFoundException
    {
        /*
        Tag.id  TagClass.id
        259     211
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.TAG_HAS_TYPE_TAGCLASS ), new CsvLineInserter()
        {
            @Override
            public Object[] transform( Object[] columnValues )
            {
                long tagNodeId = tagsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                long tagClassNodeId = tagClassesIndex.get( Long.parseLong( (String) columnValues[1] ) );
                return new Object[] { tagNodeId, tagClassNodeId };
            }

            @Override
            public void insert( Object[] columnValues )
            {
                batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1], Domain.Rel.HAS_TYPE,
                        EMPTY_MAP );
            }
        } );
    }

    private static CsvFileInserter tagClassIsSubclassOfTagClass( final String csvDataDir,
            final BatchInserter batchInserter, final TagClassesBatchIndex tagClassesIndex )
            throws FileNotFoundException
    {
        /*
        TagClass.id     TagClass.id
        211             239
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.TAGCLASS_IS_SUBCLASS_OF_TAGCLASS ),
                new CsvLineInserter()
                {
                    @Override
                    public Object[] transform( Object[] columnValues )
                    {
                        long subTagClassNodeId = tagClassesIndex.get( Long.parseLong( (String) columnValues[0] ) );
                        long tagClassNodeId = tagClassesIndex.get( Long.parseLong( (String) columnValues[1] ) );
                        return new Object[] { subTagClassNodeId, tagClassNodeId };
                    }

                    @Override
                    public void insert( Object[] columnValues )
                    {
                        batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                                Domain.Rel.IS_SUBCLASS_OF, EMPTY_MAP );
                    }
                } );
    }

    private static CsvFileInserter organisationBasedNearPlace( final String csvDataDir,
            final BatchInserter batchInserter, final OrganisationsBatchIndex organisationsIndex,
            final PlacesBatchIndex placesIndex ) throws FileNotFoundException
    {
        /*
        Organisation.id     Place.id
        00                  301
         */
        return new CsvFileInserter( new File( csvDataDir + CsvFiles.ORGANISATION_IS_LOCATED_IN_PLACE ),
                new CsvLineInserter()
                {
                    @Override
                    public Object[] transform( Object[] columnValues )
                    {
                        long organisationNodeId = organisationsIndex.get( Long.parseLong( (String) columnValues[0] ) );
                        long placeNodeId = placesIndex.get( Long.parseLong( (String) columnValues[1] ) );
                        return new Object[] { organisationNodeId, placeNodeId };
                    }

                    @Override
                    public void insert( Object[] columnValues )
                    {
                        batchInserter.createRelationship( (Long) columnValues[0], (Long) columnValues[1],
                                Domain.Rel.IS_LOCATED_IN, EMPTY_MAP );
                    }
                } );
    }

    private static class RelationshipIdsComparator implements Comparator<Object[]>
    {
        @Override
        public int compare( Object[] o1, Object[] o2 )
        {
            Long minRelOfO1 = Math.min( (long) o1[0], (long) o1[1] );
            Long minRelOfO2 = Math.min( (long) o2[0], (long) o2[1] );
            return minRelOfO1.compareTo( minRelOfO2 );
        }
    }

}