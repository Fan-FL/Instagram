package com.group10.myinstagram.Models;

public class Like {
    private static final String TAG = "Like";

    private String username;

    public Like(String username) {
        this.username = username;
    }

    public Like() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Like{" + "username='" + username + '\'' + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Like other = (Like) obj;
        if (!username.equals(other.username)) return false;
        return true;
    }
}
