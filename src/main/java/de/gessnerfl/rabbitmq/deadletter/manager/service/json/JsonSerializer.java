package de.gessnerfl.rabbitmq.deadletter.manager.service.json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class JsonSerializer {

  private final Gson gson;
  
  @Autowired
  public JsonSerializer(Gson gson){
    this.gson = gson;
  }
  
  public String toJson(Object input){
    return gson.toJson(input);
  }
  
  public <T> T fromJson(Class<T> type, String json){
    return gson.fromJson(json, type);
  }
  
}
