package com.borqs.ai.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.borqs.ai.provider.AutoInboxContent.Message;

public class MessageXmlParser {

	public List<Message> parse(InputStream is) throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(is, "utf-8");
			parser.nextTag();
			return readMessages(parser);
		} finally {
			is.close();
		}
	}

	/*package*/ List<Message> readMessages(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "root");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if ("item".equals(name)) {
				
			} else {
				
			}
		}
		
		return null;
	}

}
