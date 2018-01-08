package com.sist.naver;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * <rss>
 * 	<channel>
 * 	 <item>
 * 
 * 	 </item>
 *   <item>
 * 
 * 	 </item>
 * 	</channel>
 * 	<test1>//필요없다.
 * 	</test1>
 *  <test2>//필요없다.
 * 	</test2>
 * 
 * </rss>
 * */

@XmlRootElement
public class Rss {
	private Channel channel=new Channel();

	public Channel getChannel() {
		return channel;
	}

	@XmlElement
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	
	
}
