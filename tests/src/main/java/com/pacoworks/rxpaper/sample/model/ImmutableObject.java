
package com.pacoworks.rxpaper.sample.model;

public class ImmutableObject {
    private final String value;

    public ImmutableObject(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ImmutableObject that = (ImmutableObject)o;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ImmutableObject{" + "value='" + value + '\'' + '}';
    }
}
