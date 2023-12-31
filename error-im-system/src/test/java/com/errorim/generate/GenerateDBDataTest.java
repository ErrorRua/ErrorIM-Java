package com.errorim.generate;

import com.errorim.entity.AddFriendRequest;
import com.errorim.entity.Contact;
import com.errorim.entity.User;
import com.errorim.mapper.AddFriendRequestMapper;
import com.errorim.mapper.ContactMapper;
import com.errorim.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ErrorRua
 * @date 2022/12/02
 * @description:
 */
@SpringBootTest
public class GenerateDBDataTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ContactMapper contactMapper;

    @Autowired
    private AddFriendRequestMapper addFriendRequestMapper;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * @param :
     * @return void
     * @description: 批量生成用户数据
     * @author ErrorRua
     * @date 2022/12/2
     */
    @Test
    void generateUser() {
        Stream.generate(() -> new User() {{
            setUsername(RandomStringUtils.randomAlphabetic(5));
            setPassword(bCryptPasswordEncoder.encode("123456"));
            setSex(String.valueOf(RandomUtils.nextInt(0, 2)));
            setEmail(RandomStringUtils.randomAlphabetic(5)
                    + String.valueOf(System.currentTimeMillis()).substring(8)
                    + "@qq.com");
        }}).limit(1000).forEach(userMapper::insert);
    }

    /**
     * @param :
     * @return void
     * @description: 批量生成用户好友数据
     * @author ErrorRua
     * @date 2022/12/2
     */
    @Test
    void generateUserContact() {

        List<User> users = userMapper.selectList(null);

        List<String> ids = users.stream().map(User::getUserId).collect(Collectors.toList());


        Stream.generate(() -> new Contact() {{
            setUserId(ids.get(RandomUtils.nextInt(0, ids.size())));
            setFriendId(ids.get(RandomUtils.nextInt(0, ids.size())));
        }}).limit(2000).forEach(contact -> {
            if (!Objects.equals(contact.getUserId(), contact.getFriendId())) {
                contactMapper.insert(contact);
                contactMapper.insert(new Contact() {{
                    setUserId(contact.getFriendId());
                    setFriendId(contact.getUserId());
                }});
            }
        });
    }

    /**
     * @param :
     * @return void
     * @description: 批量生成用户好友请求数据
     * @author ErrorRua
     * @date 2022/12/2
     */
    @Test
    void generateUserContactRequest() {

        List<User> users = userMapper.selectList(null);

        List<String> ids = users.stream().map(User::getUserId).collect(Collectors.toList());

        Stream.generate(() -> new AddFriendRequest(){{
            setFromUserId(ids.get(RandomUtils.nextInt(0, ids.size())));
            setToUserId(ids.get(RandomUtils.nextInt(0, ids.size())));
            setStatus((int) (Math.pow(-1, RandomUtils.nextInt(1, 3)) * RandomUtils.nextInt(0, 2)));
        }}).limit(2000).forEach(addFriendRequest -> {
            if (!Objects.equals(addFriendRequest.getFromUserId(), addFriendRequest.getToUserId())) {
                addFriendRequestMapper.insert(addFriendRequest);
            }
        });

    }

    /**
     * @param :
     * @return void
     * @description: 批量生成用户好友请求数据(指定id)
     * @author ErrorRua
     * @date 2022/12/2
     */
    @Test
    void generateUserContactRequestSpecify() {
        String specifyId = "1595419368528044034";

        List<User> users = userMapper.selectList(null);

        List<String> ids = users.stream().map(User::getUserId).collect(Collectors.toList());

        Stream.generate(() -> new AddFriendRequest(){{
            setFromUserId(ids.get(RandomUtils.nextInt(0, ids.size())));
            setToUserId(specifyId);
            setStatus((int) (Math.pow(-1, RandomUtils.nextInt(1, 3)) * RandomUtils.nextInt(0, 2)));
        }}).limit(20).forEach(addFriendRequest -> {
            if (!Objects.equals(addFriendRequest.getFromUserId(), addFriendRequest.getToUserId())) {
                addFriendRequestMapper.insert(addFriendRequest);
            }
        });

    }
}
