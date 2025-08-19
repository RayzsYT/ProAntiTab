package de.rayzs.pat.utils;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public class LimitedList<T> {

    private ArrayDeque<T> list = new ArrayDeque<>();
    private final int maxCapacity;

    public LimitedList(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public LimitedList(LimitedList<T> list) {
        this.maxCapacity = list.maxCapacity;
        this.list = new ArrayDeque<>(list.getList());
    }

    public void add(T element) {
        if (list.size() == maxCapacity) {
            list.removeFirst();
        }

        list.add(element);
    }

    public void iterate(Consumer<T> action) {
        list.forEach(action);
    }

    public int getSize() {
        return list.size();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    private ArrayDeque<T> getList() {
        return list;
    }
}
