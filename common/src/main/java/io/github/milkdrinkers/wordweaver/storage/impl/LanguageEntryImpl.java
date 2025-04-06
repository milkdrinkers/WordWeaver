package io.github.milkdrinkers.wordweaver.storage.impl;

import io.github.milkdrinkers.wordweaver.storage.LanguageEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageEntryImpl implements LanguageEntry {
    private final Type type;
    private final String value;
    private final List<String> values;

    public LanguageEntryImpl(final Type type, final String value) {
        this.type = type;
        this.value = value;

        // Compute a compatibility value for getValues()
        this.values = new ArrayList<>(Collections.singletonList(value));
    }

    public LanguageEntryImpl(final Type type, final List<String> values) {
        this.type = type;

        // Compute a compatibility value for getValue()
        final StringBuilder builder = new StringBuilder();
        values.forEach(s -> {
            builder.append(s);
            if (!s.isEmpty())
                builder.append("\n");
        });

        this.value = builder.toString();
        this.values = values;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public boolean isCollection() {
        return type.equals(Type.LIST);
    }
}
