package com.chukong.sdk.dns;

import java.io.IOException;
import java.util.logging.Logger;

import org.xbill.DNS.Message;

/**
 * Main logic of blackhole.<br/>
 * Process the DNS query and return the answer.
 *
 * @author yihua.huang@dianping.com
 * @date Dec 14, 2012
 */

public class QueryProcesser {

    private Logger logger = Logger.getAnonymousLogger();

    public byte[] process(byte[] queryData) throws IOException {
        Message query = new Message(queryData);
        MessageWrapper responseMessage = new MessageWrapper(new Message(query
                .getHeader().getID()));
        
        AnswerHandler answer = new AnswerHandler();
        answer.handle(new MessageWrapper(query), responseMessage);
        byte[] response = null;
        if (responseMessage.hasRecord()) {
            response = responseMessage.getMessage().toWire();
            return response;
        } else {
            return null;
        }
    }
}
