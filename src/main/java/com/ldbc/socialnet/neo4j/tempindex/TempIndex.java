package com.ldbc.socialnet.neo4j.tempindex;

public interface TempIndex<K, V>
{
    public void put( K k, V v );

    public V get( K k );
}
