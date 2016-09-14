package de.gessnerfl.rabbitmq.deadletter.manager.service.rabbitmq.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.AMQP.BasicProperties;

import de.gessnerfl.rabbitmq.deadletter.manager.service.json.JsonSerializer;

@Service
public class MessageChecksum {
  
  static final String BODY_KEY = "body";
  static final String PROPERTIES_KEY = "properties";
  static final String ALGORITHM = "{sha256}";
  
  private final JsonSerializer jsonSerizizer;
  
  @Autowired
  public MessageChecksum(JsonSerializer jsonSerizizer){
    this.jsonSerizizer = jsonSerizizer;
  }

  public String createFor(BasicProperties props, byte[] body) {
    String json = toJson(props, body);
    return ALGORITHM+DigestUtils.sha256Hex(json);
  }

  String toJson(BasicProperties props, byte[] body) {
    Map<String,Object> data = new HashMap<>();
    data.put(PROPERTIES_KEY, props);
    data.put(BODY_KEY, encode(body));
    return jsonSerizizer.toJson(data);
  }

  private String encode(byte[] body) {
    return Base64.encodeBase64String(body);
  }
  
}
