package structures;

import java.util.Objects;

public class pair<T,Y>{

    public T first;
    public Y second;

    public pair()
    {}

    public pair(T first,Y second)
    {
        this.first=first;
        this.second=second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        pair<?, ?> pair = (pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}