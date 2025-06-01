package org.example.user.entity;


import org.example.chat.entity.ChatRoomMember;
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

/**
 * user 테이블 매핑 엔티티
 *
 * 관계 필드:
 * - friends       : 내가 “추가”한 친구들 (단방향)
 * - memos         : 내가 작성한 메모들 (owner 기준)
 * - chatRoomEntries: 내가 속한 채팅방 멤버십 엔트리들 (ChatRoomMember)
 * - messagesSent  : 내가 보낸 메시지들
 */
public class User {
    private Long id;
    private String name;
    private String userId;
    private String password;
    private Boolean status; // true: 온라인, false: 오프라인

    /* ===== 관계 필드 ===== */
    // 1) User → List<User> : 내가 추가한 친구들 (단방향)
    private List<User> friends;

    // 2) User → List<Memo> : 내가 작성한 메모들 (단방향: owner 가 나)
    private List<Memo> memos;

    // 3) User → List<ChatRoomMember> : 내가 참여한 채팅방 엔트리들
    private List<ChatRoomMember> chatRoomEntries;

    // 4) User → List<Message> : 내가 보낸 메시지들
    private List<Message> messagesSent;

    // 기본 생성자: 컬렉션 필드는 빈 ArrayList로 초기화
    public User() {
        this.friends = new ArrayList<>();
        this.memos = new ArrayList<>();
        this.chatRoomEntries = new ArrayList<>();
        this.messagesSent = new ArrayList<>();
    }

    // 전체 필드를 받는 생성자 (관계 필드는 외부에서 세팅해 주어도 되고, 빈 리스트로 두어도 무방)
    public User(Long id, String name, String userId, String password, Boolean status) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.password = password;
        this.status = status;

        this.friends = new ArrayList<>();
        this.memos = new ArrayList<>();
        this.chatRoomEntries = new ArrayList<>();
        this.messagesSent = new ArrayList<>();
    }

    /* ===== Getter / Setter (기본 필드) ===== */
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

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    /* ===== Getter / Setter (관계 필드) ===== */
    /**
     * 내가 “추가”한 친구 목록 (User 객체 리스트)
     */
    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    /**
     * 내가 작성한 메모들 (Memo 객체 리스트)
     */
    public List<Memo> getMemos() {
        return memos;
    }

    public void setMemos(List<Memo> memos) {
        this.memos = memos;
    }

    /**
     * 내가 참여 중인 채팅방 멤버쉽 엔트리들 (ChatRoomMember 객체 리스트)
     */
    public List<ChatRoomMember> getChatRoomEntries() {
        return chatRoomEntries;
    }

    public void setChatRoomEntries(List<ChatRoomMember> chatRoomEntries) {
        this.chatRoomEntries = chatRoomEntries;
    }

    /**
     * 내가 보낸 메시지들 (Message 객체 리스트)
     */
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
                ", password='" + password + '\'' +
                ", status=" + status +
                ", friendsCount=" + (friends != null ? friends.size() : 0) +
                ", memosCount=" + (memos != null ? memos.size() : 0) +
                ", chatRoomsCount=" + (chatRoomEntries != null ? chatRoomEntries.size() : 0) +
                ", messagesSentCount=" + (messagesSent != null ? messagesSent.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
