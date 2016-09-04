package de.gessnerfl.rabbitmq.deadletter.manager.repository;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;

@Service
public class MessageChecksum {
  
  private final Gson gson;
  
  @Autowired
  public MessageChecksum(Gson gson){
    this.gson = gson;
  }

  public String createFor(BasicProperties props, byte[] body) {
    String json = toJson(props, body);
    return "{sha256}"+DigestUtils.sha256Hex(json);
  }

  private String toJson(BasicProperties props, byte[] body) {
    Map<String,Object> data = new HashMap<>();
    data.put("properties", props);
    data.put("body", encode(body));
    return gson.toJson(data);
  }

  private String encode(byte[] body) {
    return Base64.encodeBase64String(body);
  }
  
}
