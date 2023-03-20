package com.mpay.controller;

import java.util.Map;
import com.mpay.dto.MerchantBankAccountRequest;
import com.mpay.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	AdminService adminService;

	@GetMapping("/dashboard")
	private ResponseEntity<?> getadminOrderStats() {
		Map<String, Object> orderStats = adminService.getAdminOrderStats();
		return new ResponseEntity<>(orderStats, HttpStatus.OK);
	}

    @GetMapping("/merchant-bank-accounts")
    public ResponseEntity<?> getMerchantBankAccounts(@RequestParam(value = "merchantId", required = false) Long merchantId) {
        List<MerchantBankAccountRequest> merchantBankDetails = adminService.getAllMerchantBankDetails(merchantId);
        return new ResponseEntity<>(merchantBankDetails, HttpStatus.OK);
    }

}
