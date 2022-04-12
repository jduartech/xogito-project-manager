package com.xogito.manager.fixtures;

import com.xogito.manager.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserFixture {
    public static List<User> generateUsers(String name, int times) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            User user = new User();
            user.setName(String.format("%s of %d", name, i));
            user.setEmail(String.format("%s-%d@gmail.com",name,i));
            users.add(user);
        }
        return users;
    }

    public static User getSingleUser() {
        User user = new User(1L, "Jesus Duarte", "jesus@gmail.com");
        return user;
    }
}
