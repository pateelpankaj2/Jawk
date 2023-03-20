package com.mpay.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mpay.dto.MemberTransactionResponse;
import com.mpay.dto.MerchantTransactionResponse;
import com.mpay.service.TransactionService;


@RestController
@RequestMapping(value = "/api/transactions")
public class TransactionController {

	@Autowired
	TransactionService transactionService;

	@GetMapping("/merchant")
	public ResponseEntity<?> getMerchantTrasactions(@RequestParam(name = "type", required = false) String type, @RequestParam( name = "id", required = false) Long id) {
		List<MerchantTransactionResponse> merchantTransactions = transactionService.getMerchantTransactions(type, id);
		return new ResponseEntity<>(merchantTransactions, HttpStatus.OK);
	}

	@GetMapping("/member")
	public ResponseEntity<?> getMemberTrasactions(@RequestParam(name = "type", required = false) String type, @RequestParam( name = "id", required = false) Long id) {
		List<MemberTransactionResponse> memberTransactions = transactionService.getMemberTransactions(type, id);
		return new ResponseEntity<>(memberTransactions, HttpStatus.OK);
	}

	@GetMapping("/system-earnings")
	public ResponseEntity<?> getSystemEarnings() {
		Map<String, Object> systemEarnings = transactionService.getSystemEarnings();
		return new ResponseEntity<>(systemEarnings, HttpStatus.OK);
	}

	@GetMapping("/withdraw-deposit")
	public ResponseEntity<?> getWithdrawTrasactions(@RequestParam("id") Long id, @RequestParam("type") String type) {
		return new ResponseEntity<>(transactionService.getWithdrawAndDepositTransactions(id, type), HttpStatus.OK);
	}
}
