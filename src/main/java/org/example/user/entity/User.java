package org.example.user.entity;


import org.example.chat.entity.ChatRoomMember;
import org.example.friend.entity.UserFriends;
import org.example.memo.entity.Memo;
import org.example.message.entity.Message;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * user 테이블 엔티티
 *
 * CREATE TABLE `user` (
 *   `id` INT          NOT NULL AUTO_INCREMENT,
 *   `name` VARCHAR(50) NOT NULL,
 *   `user_id` VARCHAR(50) NOT NULL,
 *   `password` VARCHAR(255) NOT NULL,
 *   `status` TINYINT(1) NOT NULL DEFAULT 0,
 *   PRIMARY KEY (`id`),
 *   UNIQUE KEY `uidx_user_userid` (`user_id`)
 * );
 *
 * ‼ 동작을 위해 DAO/Service에서 직접 관계 데이터를 채워 주어야 합니다.
 */
public class User {
    private Long id;           // PK
    private String name;      // 회원 실명
    private String userId;    // 로그인 아이디 (유니크)
    private String password;  // 해시 처리된 비밀번호
    private boolean status;   // 온라인/오프라인 상태 (true=온라인, false=오프라인)

    /***** 연관 관계 필드 *****/
    // 1) 친구 관계: user_friends 테이블에서 이 userId가 가진 친구 목록
    private List<UserFriends> friends = new ArrayList<>();

    // 2) 메모: 이 사용자가 보낸 메모 목록 (owner_id = this.id)
    private List<Memo> memosSent = new ArrayList<>();

    // 3) 메모: 이 사용자가 받은 메모 목록 (friend_id = this.id)
    private List<Memo> memosReceived = new ArrayList<>();

    // 4) 채팅방 멤버 관계: 이 사용자가 속한 chat_room_member 목록
    private List<ChatRoomMember> chatRoomMemberships = new ArrayList<>();

    // 5) 메시지: 이 사용자가 보낸 메시지 목록 (sender_id = this.id)
    private List<Message> messagesSent = new ArrayList<>();

    public User() { }

    /** 회원 가입 시 사용 (id 미지정) */
    public User(String name, String userId, String password) {
        this.name = name;
        this.userId = userId;
        this.password = password;
        this.status = false; // 기본 오프라인
    }

    /** DB에서 조회할 때 사용 (id 포함) */
    public User(Long id, String name, String userId, String password, boolean status) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.password = password;
        this.status = status;
    }

    /***** getter / setter *****/
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<UserFriends> getFriends() {
        return friends;
    }

    public void setFriends(List<UserFriends> friends) {
        this.friends = friends;
    }

    public List<Memo> getMemosSent() {
        return memosSent;
    }

    public void setMemosSent(List<Memo> memosSent) {
        this.memosSent = memosSent;
    }

    public List<Memo> getMemosReceived() {
        return memosReceived;
    }

    public void setMemosReceived(List<Memo> memosReceived) {
        this.memosReceived = memosReceived;
    }

    public List<ChatRoomMember> getChatRoomMemberships() {
        return chatRoomMemberships;
    }

    public void setChatRoomMemberships(List<ChatRoomMember> chatRoomMemberships) {
        this.chatRoomMemberships = chatRoomMemberships;
    }

    public List<Message> getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(List<Message> messagesSent) {
        this.messagesSent = messagesSent;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", password='<숨김>'" +
                ", status=" + status +
                ", friends=" + friends +
                ", memosSent=" + memosSent +
                ", memosReceived=" + memosReceived +
                ", chatRoomMemberships=" + chatRoomMemberships +
                ", messagesSent=" + messagesSent +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return id == user.id &&
                status == user.status &&
                Objects.equals(name, user.name) &&
                Objects.equals(userId, user.userId) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, userId, password, status);
    }
}
