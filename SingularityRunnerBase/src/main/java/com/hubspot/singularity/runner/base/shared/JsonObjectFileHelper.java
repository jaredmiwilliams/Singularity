package com.hubspot.singularity.runner.base.shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.hubspot.mesos.JavaUtils;

public class JsonObjectFileHelper {

  private final ObjectMapper objectMapper;
  
  @Inject
  public JsonObjectFileHelper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> Optional<T> read(Path file, Logger log, Class<T> clazz) throws IOException {
    byte[] bytes = Files.readAllBytes(file);
    
    log.trace("Read {} bytes from {}", bytes.length, file);
    
    if (bytes.length == 0) {
      return Optional.absent();
    }
    
    try {
      T object = objectMapper.readValue(bytes, clazz);
      return Optional.of(object);
    } catch (IOException e) {
      log.warn("File {} is not a valid {} ({})", file, clazz.getSimpleName(), JavaUtils.toString(bytes), e);
    }
    
    return Optional.absent();
  }
  
  public boolean writeObject(Object object, Path path, Logger log) {
    try {
      final byte[] bytes = objectMapper.writeValueAsBytes(object);
      
      log.info("Writing {} bytes of {} to {}", new Object[] { Integer.toString(bytes.length), object.toString(), path.toString() });
        
      Files.write(path, bytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    
      return true;
    } catch (Throwable t) {
      log.error("Failed writing {}", object.toString(), t);
      return false;
    }
  }
  
}