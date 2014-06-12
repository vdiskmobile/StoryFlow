package com.youqude.storyflow;

public interface StoryFlowEventHandler {

    public void handleSeviceResult(String err_msg, final int eventId, Object rlt);
}
