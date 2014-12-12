package com.chukong.sdk.dns;

import java.net.Inet6Address;
import java.net.UnknownHostException;

import org.apache.http.conn.util.InetAddressUtils;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Address;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import android.util.Log;

/**
 * @author yihua.huang@dianping.com
 * @date Dec 14, 2012
 */
public class RecordBuilder {

	private Configure configure = Configure.getConfigure();

	private int type;

	private int dclass;

	private String answer;

	private Name name;

	private int priority = 5;//configure.getMxPriory();

	private long ttl = configure.getTTL();

	public Record toRecord() throws UnknownHostException, TextParseException {
	    Log.d("taugin1", "toRecord type = " + type);
		switch (type) {
		case Type.A:
			return new ARecord(name, dclass, ttl, Address.getByAddress(answer));
		case Type.MX:
			return new MXRecord(name, dclass, ttl, priority, new Name(answer));
		case Type.PTR:
			return new PTRRecord(name, dclass, ttl, new Name(answer));
		case Type.CNAME:
			return new CNAMERecord(name, dclass, ttl, new Name(answer));
		case Type.AAAA:
		    return new AAAARecord(name, dclass, ttl, Address.getByAddress(answer));
		default:
			throw new IllegalStateException("type " + Type.string(type)
					+ " is not supported ");
		}
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public RecordBuilder type(int type) {
		this.type = type;
		return this;
	}

	/**
	 * @param dclass
	 *            the dclass to set
	 */
	public RecordBuilder dclass(int dclass) {
		this.dclass = dclass;
		return this;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public RecordBuilder name(Name name) {
		this.name = name;
		return this;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public RecordBuilder priority(int priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * @param ttl
	 *            the ttl to set
	 */
	public RecordBuilder ttl(long ttl) {
		this.ttl = ttl;
		return this;
	}

	/**
	 * @param answer
	 *            the answer to set
	 */
	public RecordBuilder answer(String answer) {
		this.answer = answer;
		return this;
	}

}
