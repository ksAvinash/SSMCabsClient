package com.labs.ssmcabs.client.helper;

public class BoardedAdapter {
    private String board_time;

    public void setBoard_time(String board_time) {
        this.board_time = board_time;
    }

    public String getBoard_time() {
        return board_time;
    }
    public BoardedAdapter(){}
    public BoardedAdapter(String board_time) {
        this.board_time = board_time;
    }
}
