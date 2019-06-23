package com.apartmentmanager.remote.hystrix;

import com.apartmentmanager.remote.FileRemoteClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FileRemoteFallbackFactory implements FallbackFactory<FileRemoteClient> {

    @Override
    public FileRemoteClient create(Throwable throwable) {
        log.error("Failed to call FileRemoteClient", throwable);
        return new FallBack();
    }

    private class FallBack implements FileRemoteClient {

        @Override
        public IdCardInfo uploadAndParse(FileForm fileForm) {
            return null;
        }

        @Override
        public FileInfo upload(FileForm fileForm) {
            return null;
        }

//        @Async
        @Override
        public void bindFiles(List<FileBindingInfo> bindingInfos) {

        }

        @Override
        public void deleteFile(String id) {

        }

        @Override
        public List<FileInfo> getFiles(String app, String module, String externalId) {
            return null;
        }
    }
}
