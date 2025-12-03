package com.ywzai.domain.agent.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface IRagService {

  void storeRagFile(String name, String tag, List<MultipartFile> files);
}
