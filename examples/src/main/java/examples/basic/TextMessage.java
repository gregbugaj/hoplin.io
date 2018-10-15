package examples.basic;

public class TextMessage
{
    private String text;

    public String getText()
    {
        return text;
    }

    public TextMessage setText(final String text)
    {
        this.text = text;
        return this;
    }

    @Override public String toString()
    {
        return text;
    }
}
