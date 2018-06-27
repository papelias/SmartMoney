package smartmoney;

public class Goal
{
    public final String name;
    public final int max;
    public final int current;
    public final double rev;
    public final double exp;

    public Goal(final String name, final int max, final int value, final double exp, final double rev)
    {
        this.name = name;
        this.max = max;
        this.current = value;
        this.rev = rev;
        this.exp = exp;
    }
    public Goal(final String name, final int max, final int value)
    {
        this.name = name;
        this.max = max;
        this.current = value;
        this.rev = 0;
        this.exp = 0;
    }
}
