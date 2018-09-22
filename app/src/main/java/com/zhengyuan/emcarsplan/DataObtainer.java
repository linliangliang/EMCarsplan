package com.zhengyuan.emcarsplan;

import com.zhengyuan.baselib.constants.Constants;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.xml.Element;
import com.zhengyuan.baselib.xmpp.ChatUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;


public enum DataObtainer {
    INSTANCE;
    private final String LOG_TAG = "DataObtainer";
    public void  sendCarsPlanMessage(String s1,String sname,final NetworkCallbacks.SimpleDataCallback callback){
        Element element = new Element("mybody");
        element.addProperty("type", "requestCarsPlan");
        element.addProperty("info",s1);
        element.addProperty("userId",sname);

        ChatUtils.INSTANCE.sendMessage(Constants.CHAT_TO_USER, element.toString(),"returnCarsPlan",
                new NetworkCallbacks.MessageListenerThinner() {
                    @Override
                    public void processMessage(Element element, Message message, Chat chat) {
                        boolean isSuccess = element.getBody() != null &&
                                !element.getBody().equals("");
                        callback.onFinish(isSuccess, "", element.getProperty("result"));
                    }
                });
    }
}