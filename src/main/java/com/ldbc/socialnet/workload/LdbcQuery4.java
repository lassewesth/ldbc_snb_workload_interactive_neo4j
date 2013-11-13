package com.ldbc.socialnet.workload;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ldbc.driver.Operation;

public class LdbcQuery4 extends Operation<List<LdbcQuery4Result>>
{
    private final long personId;
    private final Date maxDate;
    private final int durationDays;

    public LdbcQuery4( long personId, Date maxDate, int durationDays )
    {
        super();
        this.personId = personId;
        this.maxDate = maxDate;
        this.durationDays = durationDays;
    }

    public long personId()
    {
        return personId;
    }

    public Date minDate()
    {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime( maxDate );
        c.add( Calendar.DATE, -durationDays );
        return c.getTime();
    }

    public Date maxDate()
    {
        return maxDate;
    }

    public int durationDays()
    {
        return durationDays;
    }

    public long minDateAsMilli()
    {
        return minDate().getTime();
    }

    public long maxDateAsMilli()
    {
        return maxDate().getTime();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + durationDays;
        result = prime * result + ( ( maxDate == null ) ? 0 : maxDate.hashCode() );
        result = prime * result + (int) ( personId ^ ( personId >>> 32 ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        LdbcQuery4 other = (LdbcQuery4) obj;
        if ( durationDays != other.durationDays ) return false;
        if ( maxDate == null )
        {
            if ( other.maxDate != null ) return false;
        }
        else if ( !maxDate.equals( other.maxDate ) ) return false;
        if ( personId != other.personId ) return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "LdbcQuery4 [personId=" + personId + ", minDate=" + minDate() + ", maxDate=" + maxDate()
               + ", minDateAsMilli=" + minDateAsMilli() + ", maxDateAsMilli=" + maxDateAsMilli() + ", durationDays="
               + durationDays + "]";
    }

}
