import io.hbp.com.Identifier;
import org.junit.Assert;
import org.junit.Test;

public class IdentifierTests
{
    @Test
    public void shouldFindNumberOfWordsInString()
    {
        Assert.assertEquals(1, new Identifier("a", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(2, new Identifier("bb", Identifier.Type.PARAMETER).numberOfWords());

        Assert.assertEquals(1, new Identifier("one", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(2, new Identifier("twoTwo", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(3, new Identifier("threeThreeThree", Identifier.Type.PARAMETER).numberOfWords());

        Assert.assertEquals(1, new Identifier("ONE", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(2, new Identifier("TWO_TWO", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(3, new Identifier("THREE_THREE_THREE", Identifier.Type.PARAMETER).numberOfWords());

        Assert.assertEquals(1, new Identifier("One", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(2, new Identifier("OneTwo", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(3, new Identifier("OneTwoThree", Identifier.Type.PARAMETER).numberOfWords());

        Assert.assertEquals(1, new Identifier("One", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(2, new Identifier("One_Two", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(3, new Identifier("One_Two_Three", Identifier.Type.PARAMETER).numberOfWords());

        Assert.assertEquals(1, new Identifier("ONE", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(2, new Identifier("ONE_TWO", Identifier.Type.PARAMETER).numberOfWords());
        Assert.assertEquals(3, new Identifier("ONE_TWO_THREE", Identifier.Type.PARAMETER).numberOfWords());
    }
}
