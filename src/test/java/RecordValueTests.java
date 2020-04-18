import io.hbp.com.CsvWriter;
import org.junit.Test;

import org.junit.Assert;

public class RecordValueTests
{
    @Test
    public void shouldConvertPrimitiveToStringForCscRecords()
    {
        Object iValue = 0;
        Object fValue = 0.5f;
        Object lValue = 1L;
        Object bValue = false;
        Assert.assertEquals("0",     CsvWriter.toString(iValue));
        Assert.assertEquals("0.5",   CsvWriter.toString(fValue));
        Assert.assertEquals("1",     CsvWriter.toString(lValue));
        Assert.assertEquals("FALSE", CsvWriter.toString(bValue));
    }
}
