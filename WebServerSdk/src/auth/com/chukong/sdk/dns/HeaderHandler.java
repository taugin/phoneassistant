package com.chukong.sdk.dns;

import org.xbill.DNS.Flags;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;


public class HeaderHandler implements Handler {

	public boolean handle(MessageWrapper request, MessageWrapper response) {
		response.getMessage().getHeader().setFlag(Flags.QR);
		if (request.getMessage().getHeader().getFlag(Flags.RD)) {
			response.getMessage().getHeader().setFlag(Flags.RD);
		}
		Record queryRecord = request.getMessage().getQuestion();
		response.getMessage().addRecord(queryRecord, Section.QUESTION);
		return true;
	}
}
