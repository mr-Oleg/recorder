package com.example.recorder;

import java.util.Objects;

public class TagContainer {
    private int id;
    private String title;

    public TagContainer(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public TagContainer() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagContainer)) return false;
        TagContainer that = (TagContainer) o;
        return getId() == that.getId() &&
                getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle());
    }

    @Override
    public String toString() {
        return "TagContainer{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
