package com.ispw.progettoispw.Dao;

import java.util.List;

public interface ReadOnlyDao<T> {
    T read(Object...keys);
    List<T> readAll();

    void upsert(T updated);
}
