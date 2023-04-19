package com.dynatrace.easytravel.cassandra.tables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.dynatrace.easytravel.cassandra.entities.UserEntity;
import com.dynatrace.easytravel.jpa.business.User;
import com.google.common.io.BaseEncoding;

public class UserEntityTest extends SimpleTestUtil {

    @Test
    public void test() {
        User user = new User("name2", "fullName2", "email2", "pw1");
        UserEntity userEntity = new UserEntity(user);
        assertThat(userEntity.getName(), is("name2"));
        assertThat(userEntity.getEmail(), is("email2"));
        assertThat(userEntity.getFullName(), is("fullName2"));
        assertThat(userEntity.getPassword(), is(BaseEncoding.base16().lowerCase().encode("pw1".getBytes())));

        User user2 = userEntity.createModel();
        assertThat(user2, is(user));
    }
}
