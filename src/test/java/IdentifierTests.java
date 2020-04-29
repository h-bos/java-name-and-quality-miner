import io.hbp.com.Identifier;
import org.junit.Assert;
import org.junit.Test;

public class IdentifierTests
{
    @Test
    public void shouldFindNumberOfWordsInString()
    {
        Assert.assertEquals(1, new Identifier("a").numberOfWords());
        Assert.assertEquals(2, new Identifier("bb").numberOfWords());

        Assert.assertEquals(1, new Identifier("one").numberOfWords());
        Assert.assertEquals(2, new Identifier("twoTwo").numberOfWords());
        Assert.assertEquals(3, new Identifier("threeThreeThree").numberOfWords());

        Assert.assertEquals(1, new Identifier("ONE").numberOfWords());
        Assert.assertEquals(2, new Identifier("TWO_TWO").numberOfWords());
        Assert.assertEquals(3, new Identifier("THREE_THREE_THREE").numberOfWords());

        Assert.assertEquals(1, new Identifier("One").numberOfWords());
        Assert.assertEquals(2, new Identifier("OneTwo").numberOfWords());
        Assert.assertEquals(3, new Identifier("OneTwoThree").numberOfWords());

        Assert.assertEquals(1, new Identifier("One").numberOfWords());
        Assert.assertEquals(2, new Identifier("One_Two").numberOfWords());
        Assert.assertEquals(3, new Identifier("One_Two_Three").numberOfWords());

        Assert.assertEquals(1, new Identifier("ONE").numberOfWords());
        Assert.assertEquals(2, new Identifier("ONE_TWO").numberOfWords());
        Assert.assertEquals(3, new Identifier("ONE_TWO_THREE").numberOfWords());
    }
}
