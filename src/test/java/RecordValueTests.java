import io.hbp.com.RecordValue;
import org.junit.Test;

import org.junit.Assert;

public class RecordValueTests
{
    @Test
    public void shouldConvertPrimitiveToStringForCscRecords()
    {
        RecordValue iValue = new RecordValue("id", 0);
        RecordValue fValue = new RecordValue("id", 0.5f);
        RecordValue lValue = new RecordValue("id", 1L);
        RecordValue bValue = new RecordValue("id", false);
        Assert.assertEquals("0",     iValue.value);
        Assert.assertEquals("0.5",   fValue.value);
        Assert.assertEquals("1",     lValue.value);
        Assert.assertEquals("false", bValue.value);
    }
}
