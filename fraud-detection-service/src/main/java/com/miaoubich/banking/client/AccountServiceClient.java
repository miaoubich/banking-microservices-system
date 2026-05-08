package com.miaoubich.banking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service")
public interface AccountServiceClient {

    @PatchMapping("/api/accounts/{accountId}/status")
    void updateAccountStatus(@PathVariable Long accountId, @RequestParam String status);
}
