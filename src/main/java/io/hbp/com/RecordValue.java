package io.hbp.com;

public class RecordValue
{
    public String id;
    public String value;

    public RecordValue(String id, Object value)
    {
        this.id    = id;
        // Remove conflicting "," from file names and etc.
        this.value = String.valueOf(value).replace(",", "");

        if (value instanceof Boolean)
        {
            this.value = this.value.toUpperCase();
        }
    }
}
