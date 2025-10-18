package com.ywzai.domain.agent.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRagService {

    void storeRagFile(String name, String tag, List<MultipartFile> files);

}