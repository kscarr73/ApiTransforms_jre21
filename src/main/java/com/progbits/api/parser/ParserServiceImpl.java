package com.progbits.api.parser;

import com.progbits.api.ObjectParser;
import com.progbits.api.ParserService;
import com.progbits.api.exception.ApiException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author scarr
 */
public class ParserServiceImpl implements ParserService {

	public LinkedHashMap<String, ObjectParser> _parsers = new LinkedHashMap<>();

	public void addParser(ObjectParser parser, Map<String, String> svcProps) {
		_parsers.put(svcProps.get("type"), parser);
	}

	public void removeParser(ObjectParser parser, Map<String, String> svcProps) {
		_parsers.remove(svcProps.get("type"));
	}

	@Override
	public ObjectParser getParser(String type) throws ApiException {
		ObjectParser parser = _parsers.get(type);

		if (parser != null) {
			return parser.getParser();
		} else {
			throw new ApiException("Parser " + type + " does not exist.",
					  null);
		}
	}

}
