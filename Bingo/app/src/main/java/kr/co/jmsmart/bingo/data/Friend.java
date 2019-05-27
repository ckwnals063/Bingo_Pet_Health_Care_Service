package kr.co.jmsmart.bingo.data;

/**
 * Created by Administrator on 2019-01-14.
 */

public class Friend{
    public static final int TYPE_HEADER = 0 ;
    public static final int TYPE_BODY = 1 ;

    public static final int TYPE_UPSIDE_FRIEND = 10;
    public static final int TYPE_DOWNSIZE_FRIEND = 11;

    private int type;
    private String name;
    private String id;
    private String tokenId;
    private int friendType;

    public Friend(int type, String name, String id, String tokenId, int friendType) {
        this.type = type;
        this.name = name;
        this.id = id;
        this.tokenId = tokenId;
        this.friendType = friendType;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public int getFriendType() {
        return friendType;
    }
}